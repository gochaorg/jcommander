package xyz.cofe.term.ui

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.common.InputCharEvent
import xyz.cofe.term.buff.colors
import xyz.cofe.term.common.Color
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.buff._
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.ui.prop.color._
import xyz.cofe.term.ui.paint._
import conf.TextFieldColorConf
import xyz.cofe.log._

import xyz.cofe.term.ui.prop.color.colorProp2Color
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import TextField._

class TextField(using colors:TextFieldColorConf)
extends Widget 
with LocationRWProp
with SizeRWProp
with WidgetInput
with TextProperty
with ForegroundColor
with FillBackground:
  implicit val logger: Logger = LoggerFactory.getLogger("xyz.cofe.term.ui.TextField")

  val cursor = Prop.rw(0)
  cursor.onChange( repaint )

  val selection = Prop.rw(TextSelectRange(0,0))
  selection.onChange( repaint )

  val selectionFgColor = Prop.rw(colors.selectionFg)
  selectionFgColor.onChange( repaint )

  val selectionBgColor = Prop.rw(colors.selectionBg)
  selectionBgColor.onChange( repaint )

  foregroundColor = colors.foreground
  backgroundColor = colors.background

  paintStack.add { renderText }

  private def rootOf(widget:Widget):Option[RootWidget] =
    widget.toTreePath.listToLeaf.find(_.isInstanceOf[RootWidget]).map(_.asInstanceOf[RootWidget])

  private def renderText(paint: PaintCtx):Unit = 
    val colorStr = text.get
      .colors(foregroundColor, backgroundColor)
      .select(selection.get).colors(selectionFgColor.get, selectionBgColor.get)

    paint.write(0,0, (" "*size.width()).colors(Color.White, Color.Black) )
    paint.write(0,0,colorStr)

    if focus.isOwner 
    then
      paint.cursor.visible = true
      paint.cursor.position = Position(cursor.get, 0)
      rootOf(this).foreach { root => 
        val pos = Position(cursor.get + paint.bounds.absoluteOffset.x, paint.bounds.absoluteOffset.y)
        debug"remember $pos"
        root.session.remeberCursorInfo(
          pos, true
        )
      }      

  override def input(inputEvent: InputEvent): Boolean = 
    inputEvent match
      case ke : InputKeyEvent =>
        val actions = inputParser.input(ke)
        if actions.nonEmpty then
          actions.map { a => a(this) }.foldLeft( false ){ case (r,i) => r || i }
        else
          false
      case ce:InputCharEvent =>
        val actions = inputParser.input(ce)
        if actions.nonEmpty then
          actions.map { a => a(this) }.foldLeft( false ){ case (r,i) => r || i }
        else
          if ce.isAltDown()==false && ce.isControlDown()==false then
            insertString(""+ce.getChar())
            true
          else
            false
      case _ => false

  private def moveCursor(nextCursor:Int, select:Boolean):Unit =
      if !select 
      then clearSelection() 
      else
        if selection.get.size < 1
        then
          selection.set(TextSelectRange(nextCursor, cursor.get))
        else
          selection.set(selection.get.extendTo(nextCursor))
      cursor.set( nextCursor )

  def moveCursorLeft(select:Boolean=false):Boolean = 
    if cursor.get>0 
    then
      moveCursor(cursor.get-1,select)
      true
    else
      false

  def moveCursorRight(select:Boolean=false):Boolean = 
    if cursor.get<text.get.length()
    then
      moveCursor(cursor.get+1,select)
      true
    else
      false

  def moveCursorEnd(select:Boolean=false):Boolean =
    if cursor.get<text.get.length()
    then
      moveCursor(text.get.length(),select)
      true
    else
      false

  def moveCursorHome(select:Boolean=false):Boolean =
    if cursor.get>0 
    then
      moveCursor(0,select)
      false
    else
      false

  def insertString(string:String):Unit =
    if selection.get.size>0 
    then
      val selectRange = selection.get
      val lft = text.get before selectRange
      val rgt = text.get after  selectRange
      text = lft + string + rgt
      cursor.set( lft.length() + string.length() )
      clearSelection()
    else
      val (lft,rgt) = text.get.splitAt(cursor.get)
      text = (lft + string + rgt)
      moveCursorRight(false)

  def deleteLeft():Boolean =
    if selection.get.size>0 
    then
      val selectRange = selection.get
      val lft = text.get before selectRange
      val rgt = text.get after  selectRange
      text = lft + rgt
      cursor.set( lft.length() )
      clearSelection()
      true
    else
      val (lft,rgt) = text.get.splitAt(cursor.get)
      text = lft.dropRight(1) + rgt
      moveCursorLeft(false)
      true

  def deleteRight():Boolean =
    if selection.get.size>0
    then
      val selectRange = selection.get
      val lft = text.get before selectRange
      val rgt = text.get after  selectRange
      text = lft + rgt
      cursor.set( lft.length() )
      clearSelection()
      true
    else
      val (lft,rgt) = text.get.splitAt(cursor.get)
      text = lft + rgt.drop(1)
      true

  def selectAll():Boolean =
    selection.set(
      TextSelectRange(0, text.get.length())
    )
    cursor.set( text.get.length() )
    true

  def selectRange(from:Int, to:Int):Either[String,Unit] =    
    Left("not implement")

  def clearSelection():Unit =
    selection.set( selection.get.resetTo(cursor.get) )

  def copyToClipboard():Unit =
    ClipboardAWT.writeString(
      selection.get.select(text.get)
    )

  val keyStrokeMap : KeyStrokeMap[Action] =
    KeyStrokeMap(Map
      (KeyStroke.KeyEvent(KeyName.Left,     altDown=false,ctrlDown=false,shiftDown=false) -> Action.MoveCursor(Direction.Left, MoveSelection.Clear)
      ,KeyStroke.KeyEvent(KeyName.Left,     altDown=false,ctrlDown=false,shiftDown=true ) -> Action.MoveCursor(Direction.Left, MoveSelection.Extend)
      ,KeyStroke.KeyEvent(KeyName.Right,    altDown=false,ctrlDown=false,shiftDown=false) -> Action.MoveCursor(Direction.Right,MoveSelection.Clear)
      ,KeyStroke.KeyEvent(KeyName.Right,    altDown=false,ctrlDown=false,shiftDown=true ) -> Action.MoveCursor(Direction.Right,MoveSelection.Extend)
      ,KeyStroke.KeyEvent(KeyName.Home,     altDown=false,ctrlDown=false,shiftDown=false) -> Action.MoveCursor(Direction.Home, MoveSelection.Clear)
      ,KeyStroke.KeyEvent(KeyName.Home,     altDown=false,ctrlDown=false,shiftDown=true ) -> Action.MoveCursor(Direction.Home, MoveSelection.Extend)
      ,KeyStroke.KeyEvent(KeyName.End,      altDown=false,ctrlDown=false,shiftDown=false) -> Action.MoveCursor(Direction.End,  MoveSelection.Clear)
      ,KeyStroke.KeyEvent(KeyName.End,      altDown=false,ctrlDown=false,shiftDown=true ) -> Action.MoveCursor(Direction.End,  MoveSelection.Extend)
      ,KeyStroke.KeyEvent(KeyName.Delete,   altDown=false,ctrlDown=false,shiftDown=false) -> Action.Delete(DeleteWhere.Right)
      ,KeyStroke.KeyEvent(KeyName.Backspace,altDown=false,ctrlDown=false,shiftDown=false) -> Action.Delete(DeleteWhere.Left)

      ,KeyStroke.CharEvent('a',altDown=false,ctrlDown=true,shiftDown=false) -> Action.Selection(SelectionWhat.All)
      ,KeyStroke.CharEvent('c',altDown=false,ctrlDown=true,shiftDown=false) -> Action.Clipboard(ClipboardAction.Copy)
      )
    )

  val inputParser = KeyStrokeMap.InputParser[Action]( keyStrokeMap )

object TextField:
  enum Action extends ActionExec:
    case MoveCursor(direction:Direction, selection:MoveSelection) extends Action with MoveCursorOps
    case Delete(where:DeleteWhere) extends Action with DeleteOps
    case Selection(what:SelectionWhat) extends Action with SelectionOps
    case Clipboard(action:ClipboardAction) extends Action with ClipboardOps
    case Custom(exec:TextField=>Any) extends Action with CustomOps

  enum DeleteWhere:
    case Left
    case Right

  enum Direction extends ComputeCursor:
    case Left  extends Direction with ComputeCursorImpl( tf => Option.when(tf.cursor.get>0)(tf.cursor.get-1) )
    case Right extends Direction with ComputeCursorImpl( tf => Option.when(tf.cursor.get<tf.text.get.length())(tf.cursor.get+1) )
    case Home  extends Direction with ComputeCursorImpl( tf => Option.when(tf.cursor.get>0)(0) )
    case End   extends Direction with ComputeCursorImpl( tf => Option.when(tf.cursor.get<tf.text.get.length())(tf.text.get.length()) )

  enum MoveSelection:
    case Clear
    case Extend

  enum SelectionWhat:
    case All    

  enum ClipboardAction:
    case Copy

  trait ActionExec:
    def apply(textField: TextField):Boolean

  trait ComputeCursor:
    def compute(textField:TextField):Option[Int]

  trait ComputeCursorImpl( calc:TextField=>Option[Int] ) extends ComputeCursor:
    override def compute(textField: TextField): Option[Int] = 
      calc(textField)

  trait MoveCursorOps:
    self: Action.MoveCursor =>
    def apply(textField: TextField):Boolean =
      self.direction.compute(textField).map { nextCur =>
        textField.moveCursor( 
          nextCur,
          self.selection match
            case MoveSelection.Clear => false
            case MoveSelection.Extend => true   
        )
        true
      }.getOrElse(false)

  trait DeleteOps:
    self: Action.Delete =>
    def apply(textField: TextField):Boolean =
      self.where match
        case DeleteWhere.Left =>  textField.deleteLeft()
        case DeleteWhere.Right => textField.deleteRight()

  trait SelectionOps:
    self: Action.Selection =>
    def apply(textField: TextField):Boolean =
      self.what match
        case SelectionWhat.All => textField.selectAll()
        
  trait ClipboardOps:
    self: Action.Clipboard =>
    def apply(textField: TextField):Boolean =
      self.action match
        case ClipboardAction.Copy => 
          textField.copyToClipboard()
          true
        
  trait CustomOps:
    self: Action.Custom =>
    def apply(textField: TextField):Boolean =
      val e = self.exec
      if e!=null then 
        e(textField)
        true
      else
        false
      