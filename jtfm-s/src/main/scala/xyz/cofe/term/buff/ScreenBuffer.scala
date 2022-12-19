package xyz.cofe.term.buff

import xyz.cofe.term.common.Color
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size

trait ScreenBuffer:
  def width:Int
  def height:Int
  def get(x:Int,y:Int):Option[ScreenChar]
  def get(pos:Position):Option[ScreenChar] = get(pos.x, pos.y)
  def set(x:Int,y:Int,chr:ScreenChar):Either[ScreenBufferError,Unit]
  def set(pos:Position,chr:ScreenChar):Either[ScreenBufferError,Unit] = set(pos.x, pos.y, chr)
  def copy:ScreenBuffer
  def diff(buf:ScreenBuffer): Seq[CharDifference]
  def resize(width:Int,height:Int):Either[ScreenBufferError,Unit]
  def resize(size:Size):Either[ScreenBufferError,Unit] = resize(size.width, size.height)
  def cursorPos:Position
  def cursorPos_=(pos:Position):Either[ScreenBufferError,Unit]
  def cursorVisible:Boolean
  def cursorVisible_=(vis:Boolean):Either[ScreenBufferError,Unit]
  def title:String
  def title_=(str:String):Unit
  