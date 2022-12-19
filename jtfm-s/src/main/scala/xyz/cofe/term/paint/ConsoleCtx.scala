package xyz.cofe.term.paint

import xyz.cofe.term.common.Console
import xyz.cofe.term.common.Color
import xyz.cofe.term.buff.ScreenChar
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size

case class ConsoleCtx(
  console:Console,
  absOff: Position,
  size: Size
) extends PaintCtx:

  object cursor extends Cursor {
    def position: Position = 
      val p = console.getCursorPosition()
      p.move( -absOff.x, -absOff.y )
      
    def position_=(newPosition: Position): Either[String, Unit] = 
      console.setCursorPosition(absOff.move(newPosition.x, newPosition.y))
      Right(())

    var visb:Boolean = true
    def visible: Boolean = visb
    def visible_=(switchOn: Boolean): Unit = 
      visb = switchOn
      console.setCursorVisible(switchOn)
  }
  object bounds extends Bounds {
    def absoluteOffset: Position = absOff
    def clipping: Boolean = false
    def size: Size =       
      val s = console.getSize()
      Size( s.width() - absOff.x, s.height() - absOff.y )
  }

  var titleString = "???"
  def title:String = titleString
  def title_=(string:String):Unit = 
    titleString = string
    console.setTitle(titleString)

  var background:Color = Color.Black
  var foreground:Color = Color.White

  //def read(x:Int,y:Int):Option[ScreenChar]
  def write(x:Int,y:Int,chr:ScreenChar):Unit = 
    console.setCursorPosition(absOff.move(x,y))
    write(chr)
    
  def write(x:Int,y:Int,chr:Char):Unit = 
    console.setCursorPosition(absOff.move(x,y))
    write(chr)

  def write(x:Int,y:Int,string:String):Unit = 
    console.setCursorPosition(absOff.move(x,y))
    write(string)

  def write(chr:ScreenChar):Unit = 
    console.setForeground(chr.foreground)
    console.setBackground(chr.background)
    console.write(""+chr.char)
  def write(chr:Char):Unit = write(""+chr)
  def write(string:String):Unit = 
    console.setForeground(foreground)
    console.setBackground(background)
    console.write(string) 

  def context:ContextBuilder = CtxBuilder(absOff,size,false)

  case class CtxBuilder(
    absoluteOffset1: Position,
    boundsSize1: Size,
    clipping1: Boolean,
  ) extends ContextBuilder:
    def absolute(x:Int,y:Int):ContextBuilder = 
      copy(
        absoluteOffset1 = Position(x,y)
      )
    def offset(x:Int,y:Int):ContextBuilder = 
      copy(
        absoluteOffset1 = absOff.move(x,y)
      )
    def size(width:Int,height:Int):ContextBuilder = 
      require(width>=0)
      require(height>=0)
      copy( boundsSize1 = Size(width,height) )
    def clipping(clip:Boolean):ContextBuilder = 
      copy(clipping1 = clip)
    def build:PaintCtx = ConsoleCtx.this.copy(
      absOff = absoluteOffset1,
      size = boundsSize1,
      //clipping = clipping1
    )
