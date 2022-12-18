package xyz.cofe.term.paint

import xyz.cofe.term.buff.ScreenBuffer
import xyz.cofe.term.buff.ScreenChar
import xyz.cofe.term.buff.Rect
import xyz.cofe.term.common.Color
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size

trait PaintCtx:
  def cursor:Cursor
  def bounds:Bounds

  def title:String
  def title_=(string:String):Unit

  def background:Color
  def background_=(c:Color):Unit

  def foreground:Color
  def foreground_=(c:Color):Unit

  //def read(x:Int,y:Int):Option[ScreenChar]
  def write(x:Int,y:Int,chr:ScreenChar):Unit
  def write(x:Int,y:Int,chr:Char):Unit
  def write(x:Int,y:Int,string:String):Unit
  def write(chr:ScreenChar):Unit
  def write(chr:Char):Unit
  def write(string:String):Unit  

  //def context(newBound:Rect):PaintCtx

trait Cursor:
  def visible:Boolean
  def visible_=(switchOn:Boolean):Unit
  
  def position:Position
  def position_=(newPosition:Position):Either[String,Unit]


trait Bounds:
  def size:Size
  def absoluteOffset:Position

