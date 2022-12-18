package xyz.cofe.term.paint

import xyz.cofe.term.buff._
import xyz.cofe.term.buff.Rect
import xyz.cofe.term.buff.ScreenBuffer
import xyz.cofe.term.buff.ScreenChar
import xyz.cofe.term.common.Console
import xyz.cofe.term.common.Color
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size

class BasicPaintCtx(
  console: Console,
  screenBuffer: ScreenBuffer,
  absoluteOffset: Position,
  boundsSize: Size,
  clipping: Boolean
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

  override def write(string: String):Unit = ???

  override def write(chr: Char): Unit = 
    write( ScreenChar(chr, foreground, background) )

  private def moveCursor():Unit = {
    if cursorX < boundsSize.width()-1
    then cursorX += 1
    else 
      if cursorY < boundsSize.height()-1
      then 
        cursorX = 0
        cursorY += 1
  }

  override def write(chr: ScreenChar): Unit = ???

  override def write(x: Int, y: Int, string: String): Unit = ???

  override def write(x: Int, y: Int, chr: Char): Unit = 
    screenBuffer.set(x,y,ScreenChar(chr,foreground,background))

  override def write(x: Int, y: Int, chr: ScreenChar):  Unit = 
    screenBuffer.set(x,y,chr)

  //override def read(x: Int, y: Int): Option[ScreenChar] = ???
  //override def context(newBound: Rect): PaintCtx = ???
}
