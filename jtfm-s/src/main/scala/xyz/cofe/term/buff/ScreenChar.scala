package xyz.cofe.term.buff

import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Color
import xyz.cofe.term.ui.TextSelectRange

case class ScreenChar( char:Char, foreground:Color, background:Color )
case class PosScreenChar( char:ScreenChar, pos:Position )

extension (string:String)
  def foreground(color:Color):Seq[ScreenChar] =
    string.toList.map { chr => ScreenChar(chr,color,Color.Black) }
  def background(color:Color):Seq[ScreenChar] =
    string.toList.map { chr => ScreenChar(chr,Color.White,color) }
  def colors(foreground:Color,background:Color):Seq[ScreenChar] =
    string.toList.map { chr => ScreenChar(chr,foreground,background) }
  
extension (string:Seq[ScreenChar])
  def foreground(color:Color):Seq[ScreenChar] =
    string.map { ch => ch.copy(foreground = color) }

  def background(color:Color):Seq[ScreenChar] =
    string.map { ch => ch.copy(background = color)}

  def select(from:Int,to:Int):ScreenCharStrSelect =
    ScreenCharStrSelect(string,from,to)

  def select(textRange:TextSelectRange):ScreenCharStrSelect = select( textRange.from, textRange.to )

case class ScreenCharStrSelect(string:Seq[ScreenChar], from:Int, to:Int):
  def modify(update:ScreenChar=>ScreenChar):Seq[ScreenChar] =
    string.zipWithIndex.map { case (chr,idx) => 
      if idx>=from && idx<to then
        update(chr)
      else
        chr
    }

  def colors(foreground:Color, background:Color) = modify { chr => chr.copy(foreground=foreground, background=background) }
  def foreground(color:Color) = modify(_.copy(foreground = color))
  def background(color:Color) = modify(_.copy(background = color))