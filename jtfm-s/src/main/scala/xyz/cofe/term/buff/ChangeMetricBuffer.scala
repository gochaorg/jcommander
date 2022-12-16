package xyz.cofe.term.buff

import xyz.cofe.term.common.Position

class ChangeMetricBuffer(buff:ScreenBuffer) extends ScreenBuffer:
  private var changesYX:Map[Int,Set[Int]] = Map()
  def changeYXMap:Map[Int,Set[Int]] = changesYX
  def changeCount:Int = changesYX.map(_._2.size).sum
  def reset():Unit =
    changesYX = Map()
  
  def changed(x:Int,y:Int):Boolean =
    changesYX.get(y).map(xs => xs.contains(x)).getOrElse(false)

  def changed(pos:Position):Boolean = changed(pos.x, pos.y)

  def changes:List[Position] =
    changesYX.toList.flatMap { (y,xs) => 
      xs.map { x => Position(x,y) }
    }

  override def height: Int = buff.height
  override def width: Int = buff.width
  override def set(x: Int, y: Int, chr: ScreenChar): Either[ScreenBufferError, Unit] = 
    val prev = buff.get(x,y)
    if( prev!=chr ){
      changesYX = changesYX + ( y -> (changesYX.get(y).getOrElse(Set[Int]()) ++ Set(x)) )
    }
    buff.set(x,y,chr)
  override def get(x: Int, y: Int): Option[ScreenChar] = buff.get(x,y)
  override def resize(width: Int, height: Int): Either[ScreenBufferError, Unit] = buff.resize(width,height)
  override def diff(buf: ScreenBuffer): Seq[CharDifference] = buff.diff(buf)
  override def copy: ScreenBuffer = buff.copy
