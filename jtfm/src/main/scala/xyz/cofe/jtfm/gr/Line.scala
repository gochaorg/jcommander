package xyz.cofe.jtfm.gr

import xyz.cofe.jtfm.gr.Symbols.Style

case class Line( a:Point, b:Point, style: Style ) {
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
