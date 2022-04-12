package xyz.cofe.jtfm.gr

import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.gr.Symbols.Connector

/**
 * Методы отрисовки сетки линий
 */
implicit class HVLineOps( val lines:Seq[HVLine] ) {
  private def draw( gr: TextGraphics, line:HVLine, chr:Char ):Unit = {
    if line.horiz then
      ((line.a.x min line.b.x) to (line.a.x max line.b.x)).foreach { x =>
        gr.putString( x, line.a.y, ""+chr )
      }
    else if line.vert then
      ((line.a.y min line.b.y) to (line.a.y max line.b.y)).foreach { y =>
        gr.putString( line.a.x, y, ""+chr )
      }
  }

  private def draw( gr:TextGraphics, line:HVLine, ch:Connector ):Unit =
    draw( gr, line, ch.chr )

  private def draw( gr:TextGraphics, line:HVLine ):Unit =
    draw( gr, line, line.connector.map(_.chr).getOrElse('+') )

  /**
   * Отрисовать сетку
   */
  def draw( gr: TextGraphics ):Unit = {
    lines.foreach { line => draw(gr,line) }
    val l_cnt = lines.length
    for(
      i <- 0 until l_cnt;
      j <- i+1 until l_cnt
    ) lines(i).intersection(lines(j)) match {
      case Some(Intersection(p, side)) => {
        Connector.find(side).foreach( ct => {
          gr.putString(p.x, p.y, ct.chr+"" )
        })
      }
      case _ =>
    }
  }
}
