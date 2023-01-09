package xyz.cofe.term.geom

import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size

case class Rect( left:Int, top:Int, width:Int, height:Int ):
  require(width>=0)
  require(height>=0)
  lazy val right:Int = left + width
  lazy val bottom:Int = top + height
  
  def contains(x:Int,y:Int):Boolean =
    x >= left && x < (left + width) &&
    y >= top && y < (top + height)
  
  def contains(p:Position):Boolean =
    contains(p.x, p.y)

  def leftTop:Position = Position(left,top)
  def rightTop:Position = Position(left+width,top)
  def leftBottom:Position = Position(left,top+height)
  def rightBottom:Position = Position(left+width,top+height)
  def center:Position = Position(left+width/2, top+height/2)

extension (size:Size)
  def leftUpRect(pos:Position):Rect =
    Rect(pos.x, pos.y, size.width, size.height)
  def leftUpRect(x:Int,y:Int):Rect =
    Rect(x,y,size.width,size.height)
