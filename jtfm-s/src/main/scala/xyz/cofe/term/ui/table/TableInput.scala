package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.WidgetInput
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputMouseButtonEvent

import TableGridProp.ContentBlock.HeaderBlock
import xyz.cofe.term.common.MouseButton
import xyz.cofe.term.ui.isModifiersDown
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.lazyp.Prop
import xyz.cofe.term.ui.KeyStrokeMap
import xyz.cofe.term.common.InputCharEvent
import xyz.cofe.term.common.InputKeyboardEvent
import xyz.cofe.term.ui.isModifiers

import TableInput._
import conf._
import xyz.cofe.json4s3.derv._
import xyz.cofe.json4s3.stream.ast.AST
import xyz.cofe.json4s3.derv.errors.DervError
import xyz.cofe.json4s3.derv.errors.TypeCastFail

trait TableInput[A]( using tableInputConf:TableInputConf )
extends WidgetInput
with TableRowsProp[A]
with TableGridPaint[A]
:
  override def input(inputEvent: InputEvent): Boolean = 
    inputEvent match
      case me:InputMouseButtonEvent => processMouseButtonInput(me)
      case ke:InputKeyEvent  => processKeyboardInput(ke)
      case ce:InputCharEvent => processKeyboardInput(ce)
      case _ => false

  protected def processMouseButtonInput(me:InputMouseButtonEvent):Boolean =
    val matchedHeadBlock = headersBlocks.get.map { block =>
      (block.rect.contains(me.position()), block)
    }.filter((matched,_)=>matched)
     .map((_,headBlock)=>headBlock)
     .headOption.map(b => processHeaderMouseInput(me,b))

    val matchedDataBlock = dataBlocks.get.map { dataBlock => 
      (dataBlock.rect.contains(me.position()), dataBlock)
    }.filter((matched,_)=>matched)
     .map((_,dataBlock)=>dataBlock)
     .headOption
     .flatMap { dataBlock => 
       val rowVisibleOffset = me.position.y - dataBlock.rect.top
       val rowIndex = scroll.value.get + rowVisibleOffset
       rows.getAt(rowIndex).map { dataRow => 
        processCellMouseInput(me,dataRow,dataBlock.col,rowIndex) 
      }
     }

    matchedHeadBlock.orElse(matchedDataBlock).getOrElse(processDefaultMouseInput(me))

  protected def processHeaderMouseInput(me:InputMouseButtonEvent, block:HeaderBlock[A]):Boolean =
    if me.button()==MouseButton.Left && me.pressed()
    then true
    else false

  protected def processCellMouseInput(me:InputMouseButtonEvent, dataRow:A, column:Column[A,_], rowIndex:Int):Boolean =
    if me.button()==MouseButton.Left && me.pressed()
    then 
      selection.focusedIndex.set(Some(rowIndex))
      true
    else false
    
  protected def processDefaultMouseInput(me:InputMouseButtonEvent):Boolean =
    if me.button()==MouseButton.Left && me.pressed()
    then true
    else false

  
  lazy val keyStrokeMap = KeyStrokeMap[()=>Unit](predefKeyStrokes)
  private lazy val keyStrokeParser = KeyStrokeMap.InputParser[()=>Unit](keyStrokeMap)

  protected def processKeyboardInput(ke:InputKeyboardEvent):Boolean =
    var matched = false
    keyStrokeParser.input(ke){ action => 
      matched = true
      action() 
    }
    matched

  private def predefKeyStrokes : Map[KeyStroke,Set[()=>Unit]] = 
    tableInputConf.keyStrokeActionMap(this).mapValues(v=>Set(v)).toMap

  private def scrollHeight:Int = 
    val (dataYMin, dataYMax) = dataYPos.get
    dataYMax - dataYMin

  def moveTo(select:MoveSelection)(focusedIndex:Int=>Int):Unit =
    selection.focusedIndex.get match
      case None => 
        renderDataRows.get.headOption.foreach { dataRow => 
          moveTo(select, dataRow.index)
        }
      case Some(idx) =>
        moveTo(select, focusedIndex(idx))

  def moveTo(select:MoveSelection, nextIndex:Int):Unit =
    if nextIndex >= 0 && nextIndex < rows.size
    then
      select match
        case MoveSelection.NoChange => 
        case MoveSelection.Target => 
          selection.set(nextIndex)
        case MoveSelection.Include =>      
          selection.focusedIndex.get match
            case None => 
            case Some(focusedIndex) =>
              selection.indexes.include(
                (( nextIndex min focusedIndex ) until ((nextIndex max focusedIndex)+1))
              )
        case MoveSelection.Exclude =>
          selection.focusedIndex.get match
            case None => 
            case Some(focusedIndex) =>
              selection.indexes.exclude(
                (( nextIndex min focusedIndex ) until ((nextIndex max focusedIndex)+1))
              )
        case MoveSelection.Invert => 
          selection.focusedIndex.get match
            case None => 
            case Some(focusedIndex) =>
              (( nextIndex min focusedIndex ) 
                 until 
              ((nextIndex max focusedIndex)+1)).foreach( ridx =>
                if selection.indexes.contains(ridx) 
                then selection.indexes.exclude(ridx)
                else selection.indexes.include(ridx)
              )

      selection.focusedIndex.set(Some(nextIndex))
      scrollTo(nextIndex)

  private def scrollTo( rowIndex:Int ):Unit =
    val (dataYMin, dataYMax) = dataYPos.get
    val scrollHeight = dataYMax - dataYMin
    val dataMinVisibleIndex = scroll.value.get
    val dataMinInVisibleTailIndex = scroll.value.get + scrollHeight
    if rowIndex < dataMinVisibleIndex then
      scroll.value.set(rowIndex)            
    else if rowIndex >= (dataMinInVisibleTailIndex) then
      scroll.value.set( rowIndex - scrollHeight + 1 )

  def executorOf( action:Action ):()=>Unit =
    action match
      case Action.Selection(what) => what match
        case SelectWhat.All => ()=>{
          selection.indexes.include( (0 until rows.size) )
        }
        case SelectWhat.Invert => ()=>{
          (0 until rows.size).foreach { ridx =>
            if selection.indexes.contains(ridx)
            then selection.indexes.exclude(ridx)
            else selection.indexes.include(ridx)
          }
        }
        case SelectWhat.Clear => ()=>{
          selection.indexes.clear()
        }
      case Action.FocusSelection(what) => what match
        case FocusSelAction.Include => () => {
          selection.focusedIndex.get.foreach { ridx =>
            selection.indexes.include(ridx)
          }
        }
        case FocusSelAction.Exclude => () => {
          selection.focusedIndex.get.foreach { ridx =>
            selection.indexes.exclude(ridx)
          }
        }
        case FocusSelAction.Invert => () => {
          selection.focusedIndex.get.foreach { ridx =>
            if selection.indexes.contains(ridx)
            then selection.indexes.exclude(ridx)
            else selection.indexes.include(ridx)
          }
        }      
      case Action.FocusMove(direction, selection) => 
        () => {
          moveTo(selection) { focusIdx => 
            direction match
              case FocusMoveDirection.Up => 
                focusIdx-1
              case FocusMoveDirection.Down =>
                focusIdx+1
              case FocusMoveDirection.PageUp =>
                focusIdx-(scrollHeight-1) max 0
              case FocusMoveDirection.PageDown =>
                focusIdx+(scrollHeight-1) min rows.size-1
              case FocusMoveDirection.Home =>
                0
              case FocusMoveDirection.End =>
                rows.size-1
          }
        }

  def executorOf( actions:List[Action] ):()=>Unit =
    val execs = actions.map(executorOf)
    ()=>{
      execs.foreach( e => e() )
    }

object TableInput:
  enum MoveSelection:
    case NoChange
    case Include
    case Exclude
    case Invert
    case Target

  object MoveSelection:
    given ToJson[MoveSelection] with
      override def toJson(v: MoveSelection): Option[AST] = 
        summon[ToJson[String]].toJson( v match
          case NoChange => "no-change"
          case Include => "include"
          case Exclude => "exclude"
          case Invert => "invert"
          case Target => "target"
         )

    given FromJson[MoveSelection] with
      override def fromJson(j: AST): Either[DervError, MoveSelection] = 
        summon[FromJson[String]].fromJson(j).flatMap {
          case "no-change" => Right(MoveSelection.NoChange)
          case "include" => Right(MoveSelection.Include)
          case "exclude" => Right(MoveSelection.Exclude)
          case "invert" => Right(MoveSelection.Invert)
          case "target" => Right(MoveSelection.Target)
          case str => Left(TypeCastFail(s"can't cast to MoveSelection from '$str'"))
        }

  enum FocusMoveDirection:
    case Up, Down, PageUp, PageDown, Home, End

  object FocusMoveDirection:
    given ToJson[FocusMoveDirection] with
      override def toJson(v: FocusMoveDirection): Option[AST] = 
        summon[ToJson[String]].toJson( v match
          case Up => "up"
          case Down => "down"
          case PageUp => "page-up"
          case PageDown => "page-down"
          case Home => "home"
          case End => "end"
         )

    given FromJson[FocusMoveDirection] with
      override def fromJson(j: AST): Either[DervError, FocusMoveDirection] = 
        summon[FromJson[String]].fromJson(j).flatMap {
          case "up" => Right(FocusMoveDirection.Up)
          case "down" => Right(FocusMoveDirection.Down)
          case "page-up" => Right(FocusMoveDirection.PageUp)
          case "page-down" => Right(FocusMoveDirection.PageDown)
          case "home" => Right(FocusMoveDirection.Home)
          case "end" => Right(FocusMoveDirection.End)
          case str => Left(TypeCastFail(s"can't cast to FocusMoveDirection from '$str'"))
        }

  enum FocusSelAction:
    case Include, Exclude, Invert

  object FocusSelAction:
    given ToJson[FocusSelAction] with
      override def toJson(v: FocusSelAction): Option[AST] = 
        summon[ToJson[String]].toJson( v match
          case Include => "include"
          case Exclude => "exclude"
          case Invert => "invert"
         )

    given FromJson[FocusSelAction] with
      override def fromJson(j: AST): Either[DervError, FocusSelAction] = 
        summon[FromJson[String]].fromJson(j).flatMap {
          case "invert" => Right(FocusSelAction.Invert)
          case "include" => Right(FocusSelAction.Include)
          case "exclude" => Right(FocusSelAction.Exclude)
          case str => Left(TypeCastFail(s"can't cast to FocusSelAction from '$str'"))
        }

  enum SelectWhat:
    case All, Invert, Clear

  object SelectWhat:
    given ToJson[SelectWhat] with
      override def toJson(v: SelectWhat): Option[AST] = 
        summon[ToJson[String]].toJson( v match
          case All => "all"
          case Clear => "clear"
          case Invert => "invert"
         )

    given FromJson[SelectWhat] with
      override def fromJson(j: AST): Either[DervError, SelectWhat] = 
        summon[FromJson[String]].fromJson(j).flatMap {
          case "invert" => Right(SelectWhat.Invert)
          case "all" => Right(SelectWhat.All)
          case "clear" => Right(SelectWhat.Clear)
          case str => Left(TypeCastFail(s"can't cast to SelectWhat from '$str'"))
        }

  enum Action:
    case Selection(what:SelectWhat)
    case FocusSelection(what:FocusSelAction)
    case FocusMove(direction:FocusMoveDirection, selection:MoveSelection)

  case class KeyStrokeBinding(
    keyStroke: KeyStroke,
    actions: List[Action]
  )
  
  case class MouseTrigger(
    button:  MouseButton,
    pressed: Boolean
  )

  object MouseTrigger:
    given ToJson[MouseTrigger] with
      override def toJson(v: MouseTrigger): Option[AST] = 
        Some(AST.JsObj(List(
          "button" -> AST.JsStr({ v.button match
            case MouseButton.Left => "left"
            case MouseButton.Right => "right"
            case MouseButton.Middle => "middle"
           }),
           "pressed" -> AST.JsBool(v.pressed)
        )))

    given FromJson[MouseTrigger] with
      override def fromJson(json: AST): Either[DervError, MouseTrigger] = 
        json match
          case js @ AST.JsObj(fields) =>
            js.get("button").flatMap { 
              case AST.JsStr("left")   => Some(MouseButton.Left)
              case AST.JsStr("right")  => Some(MouseButton.Right)
              case AST.JsStr("middle") => Some(MouseButton.Middle)
              case _ => None
            }.map( x => Right(x) )
             .getOrElse(
                Left(TypeCastFail(
                  s"can't cast button field from ${js.get("button")}"
                ):DervError)
              )
             .flatMap { mb => 
               js.get("pressed").flatMap {  
                case AST.JsBool(x) => Some(x)
                case _ => None
               }
               .map( x => Right(x) )
               .getOrElse(Right(true))
               .map { pressed => 
                  MouseTrigger(mb, pressed)
                }
             }
          case _ =>
            Left(TypeCastFail("button field not found"):DervError)
        
  