package xyz.cofe.term.paint

import xyz.cofe.term.geom._
import Symbols.Connector

extension (lines:Seq[HVLine])
  private def draw( gr: PaintCtx, line:HVLine, chr:Char ):Unit = {
    if line.horiz then
      ((line.a.x min line.b.x) to (line.a.x max line.b.x)).foreach { x =>
        gr.write(x, line.a.y, ""+chr)
      }
    else if line.vert then
      ((line.a.y min line.b.y) to (line.a.y max line.b.y)).foreach { y =>
        gr.write( line.a.x, y, ""+chr )
      }
  }

  private def draw( gr:PaintCtx, line:HVLine, ch:Connector ):Unit =
    draw( gr, line, ch.chr )

  private def draw( gr:PaintCtx, line:HVLine ):Unit =
    draw( gr, line, line.connector.map(_.chr).getOrElse('+') )

  def draw(gr: PaintCtx):Unit = 
    lines.foreach { line => draw(gr,line) }
    val l_cnt = lines.length
    for(
      i <- 0 until l_cnt;
      j <- i+1 until l_cnt
    ) lines(i).intersection(lines(j)) match {
      case Some(Intersection(p, side)) => {
        Connector.find(side).foreach( ct => {
          gr.write(p.x, p.y, ct.chr+"" )
        })
      }
      case _ =>
    }