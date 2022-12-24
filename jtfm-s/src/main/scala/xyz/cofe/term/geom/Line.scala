package xyz.cofe.term.geom

import xyz.cofe.term.common.Position

import Symbols.Style

/**
 * Линия
 * @param a исходная точка
 * @param b целевая точка
 * @param style тип линии
 */
case class Line( 
  a:Position, 
  b:Position, 
  style: Style 
) {
  lazy val diagonal: Boolean = {
    val d = a diff b
    !( d.x!=0 && d.y!=0 )
  }
  lazy val dot:Boolean = {
    val d = a diff b
    d.x==0 && d.y==0
  }
  def toHVLine():Option[HVLine] = {
    if dot then
      None
    else if diagonal then
      None
    else
      Some( HVLine(a,b,style) )
  }
}
