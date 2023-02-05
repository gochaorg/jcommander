package xyz.cofe.term.geom

import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size

case class Rect( left:Int, top:Int, width:Int, height:Int ):
  require(width>=0,  s"width to small = $width")
  require(height>=0, s"height to small = $height")
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
  def size:Size = Size(width,height)
  def size(newSize:Size):Rect =
    Rect(left,top, newSize.width(), newSize.height())
  def size(newSize:(Int,Int)):Rect =
    Rect(left,top, newSize._1, newSize._2)

extension (size:Size)
  def leftUpRect(pos:Position):Rect =
    Rect(pos.x, pos.y, size.width, size.height)
  def leftUpRect(x:Int,y:Int):Rect =
    Rect(x,y,size.width,size.height)

extension (twoPoints:(Position,Position))
  def rect:Rect =
    val p0 = twoPoints._1
    val p1 = twoPoints._2
    val xMin = p0.x min p1.x
    val yMin = p0.y min p1.y
    val xMax = p0.x max p1.x
    val yMax = p0.y max p1.y
    val w = xMax - xMin
    val h = yMax - yMin
    Rect(
      xMin, yMin,
      w, h
    )