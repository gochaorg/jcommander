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
import xyz.cofe.log._

import xyz.cofe.term.ui.prop.color.colorProp2Color
import org.slf4j.Logger
import org.slf4j.LoggerFactory
class TextField 
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

  val selectionFgColor = Prop.rw(Color.Black)
  selectionFgColor.onChange( repaint )

  val selectionBgColor = Prop.rw(Color.White)
  selectionBgColor.onChange( repaint )

  paintStack.add { renderText }

  private def rootOf(widget:Widget):Option[RootWidget] =
    widget.toTreePath.listToLeaf.find(_.isInstanceOf[RootWidget]).map(_.asInstanceOf[RootWidget])

  private def renderText(paint: PaintCtx):Unit = 
    val colorStr = text.get
      .colors(foregroundColor, backgroundColor)
      .select(selection.get).colors(selectionFgColor.get, selectionBgColor.get)

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
        ke.getKey() match
          case KeyName.Left =>
            if ke.isModifiers(false,false,false) 
            then moveCursorLeft()
            else if ke.isModifiers(altDown = false, controlDown = false, shiftDown = true)
              then moveCursorLeft(true)
              else false
          case KeyName.Right =>
            if ke.isModifiers(false,false,false) 
            then moveCursorRight()
            else if ke.isModifiers(altDown = false, controlDown = false, shiftDown = true)
              then moveCursorRight(true)
              else false
          case KeyName.Home =>
            if ke.isModifiers(false,false,false) 
            then moveCursorHome()
            else if ke.isModifiers(altDown = false, controlDown = false, shiftDown = true)
              then moveCursorHome(true)
              else false
          case KeyName.End =>
            if ke.isModifiers(false,false,false) 
            then moveCursorEnd()
            else if ke.isModifiers(altDown = false, controlDown = false, shiftDown = true)
              then moveCursorEnd(true)
              else false
          case KeyName.Delete =>
            if ke.isModifiers(false,false,false) 
            then deleteRight()
            else false
          case KeyName.Backspace =>
            if ke.isModifiers(false,false,false) 
            then deleteLeft()
            else false
          case _ => false
      case ce:InputCharEvent =>
        if ce.isControlDown() && ce.getChar()=='a' then          
          selectAll()
        else if ce.getChar()=='c' && ce.isModifiers(altDown = false, controlDown = true, shiftDown = false) then
          copyToClipboard()
          true
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