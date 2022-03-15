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
}

object Size2D {
  def apply(rect:Rect):Size2D = Size2D(rect.width, rect.height)
  implicit def terminalSize2Size2D( termSize:TerminalSize ):Size2D = Size2D(termSize.getColumns, termSize.getRows)
  implicit def size2D2TerminalSize( size2D:Size2D ):TerminalSize = new TerminalSize(size2D.width, size2D.height)
}