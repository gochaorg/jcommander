package xyz.cofe.term.paint

import xyz.cofe.term.buff._
import xyz.cofe.term.buff.Rect
import xyz.cofe.term.buff.ScreenBuffer
import xyz.cofe.term.buff.ScreenChar
import xyz.cofe.term.common.Console
import xyz.cofe.term.common.Color
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size

import xyz.cofe.term.paint.BasicPaintCtx._

case class BasicPaintCtx(
  screenBuffer: ScreenBuffer,
  absoluteOffset: Position,
  boundsSize: Size,
  clipping: Boolean,
  specChars: SpecChar = SpecChar.Skip,  
  lineFeedChar: EscapeAction = EscapeAction.CRLF,
  carriageReturnChar: EscapeAction = EscapeAction.Skip,
  tabChar: EscapeAction = EscapeAction.TabAligned(8),
  backspaceChar: EscapeAction = EscapeAction.MovePrev,
) 
extends PaintCtx {
  var cursorX: Int = 0
  var cursorY: Int = 0
  override def title: String = screenBuffer.title

  override def title_=(string: String): Unit = 
    screenBuffer.title = string

  override def bounds: Bounds = boundsInstance

  object boundsInstance extends Bounds {
    def absoluteOffset: Position = BasicPaintCtx.this.absoluteOffset
    def size: Size = boundsSize
    def clipping: Boolean = BasicPaintCtx.this.clipping
  }

  override def cursor: Cursor = cursorInstance

  object cursorInstance extends Cursor {
    def position: Position = Position(cursorX,cursorY)
    def position_=(newPosition: Position): Either[String, Unit] = ???

    def visible: Boolean = screenBuffer.cursorVisible
    def visible_=(switchOn: Boolean): Unit = 
      screenBuffer.cursorVisible = switchOn
  }

  var foreground : Color = Color.White
  var background : Color = Color.Black

  override def write(string: String):Unit = 
    write(cursorX,cursorY,string)

  override def write(chr: Char): Unit = 
    write( ScreenChar(chr, foreground, background) )

  override def write(chr: ScreenChar): Unit = 
    val code = chr.char.toInt
    if code>=32 then
      screenBuffer.set(cursorX + absoluteOffset.x,cursorY + absoluteOffset.y,chr)
      moveNext()
    else code match
      case 10 /* \n */ => execute(lineFeedChar,chr)
      case 13 /* \r */ => execute(carriageReturnChar,chr)
      case  9 /* \t */ => execute(tabChar,chr)
      case  8 /* \b backspace */ =>  execute(backspaceChar,chr)
      case _: Int => 
        write(cursorX,cursorY,chr)
        moveNext()

  override def write(x: Int, y: Int, string: String): Unit = 
    cursorX = x
    cursorY = y
    string.foreach { c => write(ScreenChar(c,foreground,background)) }

  override def write(x: Int, y: Int, chr: Char): Unit = 
    write(x,y,ScreenChar(chr,foreground,background))

  override def write(x: Int, y: Int, schr: ScreenChar):  Unit = 
    if clipping && ( x<0 || x>=boundsSize.width || y<0 || y>boundsSize.height )
    then
      ()
    else
      val code = schr.char.toInt
      if code>=32 then
        screenBuffer.set(x+absoluteOffset.x,y+absoluteOffset.y,schr)
      else
        specChars match
          case SpecChar.Skip =>
          case SpecChar.PrintDummy(dummy) => 
            screenBuffer.set(
              x+absoluteOffset.x,
              y+absoluteOffset.y,
              schr.copy(char = dummy))
          case SpecChar.Print => 
            screenBuffer.set(
              x+absoluteOffset.x,
              y+absoluteOffset.y,
              schr)

  private def moveNext():Unit = 
    if cursorX<boundsSize.width-1
    then cursorX+=1
    else if cursorY<boundsSize.height-1
      then
        cursorY += 1
        cursorX = 0

  private def movePrev():Unit = 
    if cursorX>0 
    then
      cursorX -= 1
    else
      if cursorY>0 then
        cursorY -= 1
        cursorX = boundsSize.width() - 1

  private def execute(action:EscapeAction, schr:ScreenChar):Unit = 
    action match
      case EscapeAction.Print => 
        screenBuffer.set(
              cursorX+absoluteOffset.x,
              cursorY+absoluteOffset.y,
              schr)
        moveNext()
      case EscapeAction.LineFeed =>
        if cursorY<boundsSize.height-1 then
          cursorY += 1
      case EscapeAction.CarriageReturn =>
        cursorX = 0
      case EscapeAction.CRLF =>
        cursorX = 0
        if cursorY<boundsSize.height-1 then
          cursorY += 1
      case EscapeAction.Tab(tabSize) =>
        write(" "*tabSize)
      case EscapeAction.TabAligned(tabSize) =>
        if tabSize>0 then
          if tabSize>1 then
            val tabRest = cursorX % tabSize
            if tabRest>0 then
              write(" "*(tabSize - tabRest))
          else
            write(" ")
      case EscapeAction.MovePrev =>
        movePrev()
      case EscapeAction.MoveNext =>
        moveNext()
      case EscapeAction.Skip =>
    
}

object BasicPaintCtx:
  enum SpecChar:
    case Skip
    case PrintDummy(dummy:Char='?')
    case Print

  enum EscapeAction:
    case Print
    case LineFeed // \n 10
    case CarriageReturn // \r 13
    case CRLF
    case Tab(tabSize:Int)
    case TabAligned(tabSize:Int)
    case MovePrev
    case MoveNext
    case Skip
