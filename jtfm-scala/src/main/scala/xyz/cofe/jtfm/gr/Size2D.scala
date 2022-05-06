package xyz.cofe.jtfm.gr

import com.googlecode.lanterna.TerminalSize

final case class Size2D(width: Int, height:Int) {
  require(width>=0)
  require(height>=0)
  
  def include( p:Point, inc_right: Boolean, inc_bottom:Boolean ):Boolean =
    if( inc_right ){
      if( inc_bottom ){
        p.x>=0 && p.x<=width && p.y>=0 && p.y<=height
      }else{
        p.x>=0 && p.x<=width && p.y>=0 && p.y<height
      }
    }else{
      if( inc_bottom ){
        p.x>=0 && p.x<width && p.y>=0 && p.y<=height
      }else{
        p.x>=0 && p.x<width && p.y>=0 && p.y<height
      }
    }
  
  def include( p:Point ):Boolean = include(p, false, false)

  def align( str:String, halign:Align ):Seq[String] = {
    str.split("\\r\\n|\\n").map { s => 
      if( s.length<width ){
        halign match {
          case Align.Begin =>
            val rr:Int = (width) - s.length
            s + " ".repeat(rr)
          case Align.Center =>
            val rr:Int = (width) - s.length
            val la:Int = rr / 2
            val ra:Int = rr - la
            " ".repeat(la) + s + " ".repeat(ra)
          case Align.End =>
            val rr:Int = (width) - s.length
            " ".repeat(rr) + s
        }
      }else if( s.length>width ){
        s.substring(0, width)
      }else{
        s
      }
    }
  }

  def align( str:String, halign:Align, valign:Align ):Seq[String] = {
    if height<=0 then
      List()
    else
      val lines = align(str,halign)
      if lines.length<height then
        var res = List[String]()
        valign match {
          case Align.Begin =>
            res = lines.toList
            (0 until (height - lines.length)).foreach { _ =>
              res = res :+ " ".repeat(width)
            }
            res
          case Align.Center =>
          case Align.End =>
            (0 until (height - lines.length)).foreach { _ =>
              res = res :+ " ".repeat(width)
            }
            res = lines.toList
            res
        }
        res
      else if lines.length==height then
        lines
      else
        lines.take(height)
  }
}

object Size2D {
  def apply(rect:Rect):Size2D = Size2D(rect.width, rect.height)
  implicit def terminalSize2Size2D( termSize:TerminalSize ):Size2D = Size2D(termSize.getColumns, termSize.getRows)
  implicit def size2D2TerminalSize( size2D:Size2D ):TerminalSize = new TerminalSize(size2D.width, size2D.height)
}