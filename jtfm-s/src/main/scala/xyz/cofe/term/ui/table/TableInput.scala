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

trait TableInput[A]
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
    println(s"${me}")
    if me.button()==MouseButton.Left && me.pressed()
    then 
      selection.set(rowIndex)
      selection.focusedIndex.set(Some(rowIndex))
      true
    else false
    
  protected def processDefaultMouseInput(me:InputMouseButtonEvent):Boolean =
    if me.button()==MouseButton.Left && me.pressed()
    then true
    else false

  
  lazy val keyStrokeMap = KeyStrokeMap[()=>Unit](predefKeyStrokes)
  private lazy val keyStrokeParser = KeyStrokeMap.KeyStrokeInputParser[()=>Unit](keyStrokeMap)

  protected def processKeyboardInput(ke:InputKeyboardEvent):Boolean =
    var matched = false
    keyStrokeParser.input(ke){ action => 
      matched = true
      action() 
    }
    matched

  private lazy val predefKeyStrokes : Map[KeyStroke,Set[()=>Unit]] = Map(
    KeyStroke.KeyEvent(KeyName.Down,     altDown=false, ctrlDown=false, shiftDown=false) -> Set(this.moveDown),
    KeyStroke.KeyEvent(KeyName.Up,       altDown=false, ctrlDown=false, shiftDown=false) -> Set(this.moveUp),
    KeyStroke.KeyEvent(KeyName.PageDown, altDown=false, ctrlDown=false, shiftDown=false) -> Set(this.movePageDown),
    KeyStroke.KeyEvent(KeyName.PageUp,   altDown=false, ctrlDown=false, shiftDown=false) -> Set(this.movePageUp),
    KeyStroke.KeyEvent(KeyName.Home,     altDown=false, ctrlDown=false, shiftDown=false) -> Set(this.moveHome),
    KeyStroke.KeyEvent(KeyName.End,      altDown=false, ctrlDown=false, shiftDown=false) -> Set(this.moveEnd),

    KeyStroke.KeyEvent(KeyName.Down,     altDown=false, ctrlDown=false, shiftDown=true)  -> Set(this.moveDownWithSelect),
    KeyStroke.KeyEvent(KeyName.Up,       altDown=false, ctrlDown=false, shiftDown=true)  -> Set(this.moveUpWithSelect),
    KeyStroke.KeyEvent(KeyName.PageDown, altDown=false, ctrlDown=false, shiftDown=true)  -> Set(this.movePageDownWithSelect),
    KeyStroke.KeyEvent(KeyName.PageUp,   altDown=false, ctrlDown=false, shiftDown=true)  -> Set(this.movePageUpWithSelect),
    KeyStroke.KeyEvent(KeyName.Home,     altDown=false, ctrlDown=false, shiftDown=true)  -> Set(this.moveHomeWithSelect),
    KeyStroke.KeyEvent(KeyName.End,      altDown=false, ctrlDown=false, shiftDown=true)  -> Set(this.moveEndWithSelect),

    KeyStroke.KeyEvent(KeyName.Insert,   altDown=false, ctrlDown=false, shiftDown=false) -> Set(this.moveDownWithSelect),

    KeyStroke.CharEvent('a', altDown=false, ctrlDown=true ,shiftDown=false) -> Set(this.selectAll),
  )

  def selectAll():Unit =    
    selection.indexes.include( (0 until rows.size) )

  def moveDown():Unit =
    moveTo(MoveSelection.Target) { focusIdx => focusIdx+1 }

  def moveDownWithSelect():Unit =
    moveTo(MoveSelection.Include) { focusIdx => focusIdx+1 }

  def moveUp():Unit =
    moveTo(MoveSelection.Target) { focusIdx => focusIdx-1 }

  def moveUpWithSelect():Unit =
    moveTo(MoveSelection.Include) { focusIdx => focusIdx-1 }

  def movePageDown():Unit =
    val (dataYMin, dataYMax) = dataYPos.get
    val scrollHeight = dataYMax - dataYMin
    moveTo(MoveSelection.Target) { focusIdx => focusIdx+(scrollHeight-1) min rows.size-1 }

  def movePageDownWithSelect():Unit =
    val (dataYMin, dataYMax) = dataYPos.get
    val scrollHeight = dataYMax - dataYMin
    moveTo(MoveSelection.Include) { focusIdx => focusIdx+(scrollHeight-1) min rows.size-1 }

  def movePageUp():Unit =
    val (dataYMin, dataYMax) = dataYPos.get
    val scrollHeight = dataYMax - dataYMin
    moveTo(MoveSelection.Target) { focusIdx => focusIdx-(scrollHeight-1) max 0 }

  def movePageUpWithSelect():Unit =
    val (dataYMin, dataYMax) = dataYPos.get
    val scrollHeight = dataYMax - dataYMin
    moveTo(MoveSelection.Include) { focusIdx => focusIdx-(scrollHeight-1) max 0 }

  def moveHome():Unit =
    moveTo(MoveSelection.Target) { _ => 0 }

  def moveHomeWithSelect():Unit =
    moveTo(MoveSelection.Include) { _ => 0 }

  def moveEnd():Unit =
    moveTo(MoveSelection.Target) { _ => rows.size-1 }

  def moveEndWithSelect():Unit =
    moveTo(MoveSelection.Include) { _ => rows.size-1 }

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

object TableInput:
  enum MoveSelection:
    case NoChange
    case Include
    case Target