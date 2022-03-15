package xyz.cofe.jtfm.wid.cmpt

import org.junit.Test
import xyz.cofe.jtfm.gr.{Point,HVLine,Intersection}
import xyz.cofe.jtfm.gr.Symbols.Style.{Single=>S, Double=>D}
import xyz.cofe.jtfm.gr.Symbols.StyledSide
import xyz.cofe.jtfm.gr.Symbols.Connector

class LineTest {
  @Test
  def canvas():Unit = {
    val canvas1 = Canvas(40,20)
    canvas1.put( 0,0, 'a' )
    canvas1.put( 1,0, 'b' )
    canvas1.put( canvas1.width-1,canvas1.height-1, 'x' )
    println( canvas1 )
  }
  
  lazy val lines = {
    HVLine( Point(1,1), Point(7,1), S ) ::
      HVLine( Point(2,0), Point(2,3), S ) ::
      HVLine( Point(1,3), Point(7,3), S ) ::
      HVLine( Point(1,3), Point(1,7), S ) ::
      HVLine( Point(1,5), Point(3,5), S ) ::
      HVLine( Point(3,3), Point(3,5), S ) ::
      HVLine( Point(7,1), Point(7,6), S ) ::
      HVLine( Point(1,7), Point(4,7), S ) ::
      HVLine( Point(4,6), Point(9,6), D ) ::
      HVLine( Point(4,8), Point(9,8), D ) ::
      HVLine( Point(4,10), Point(9,10), D ) ::
      HVLine( Point(4,6), Point(4,10), D ) ::
      HVLine( Point(6,6), Point(6,10), D ) ::
      HVLine( Point(9,6), Point(9,10), D ) ::
      HVLine( Point(9,9), Point(10,9), S ) ::
      HVLine( Point(10,9), Point(10,11), S ) ::
      HVLine( Point(8,11), Point(10,11), S ) ::
      HVLine( Point(8,10), Point(8,11), S ) ::
      Nil
  }
  
  @Test
  def isect01():Unit = {
    val l1 = HVLine( Point(1,1), Point(7,1), S )
    val l2 = HVLine( Point(2,0), Point(2,3), S )
    l1.intersection(l2) match {
      case Some(p) => println(s"intersect ${p}")
        println("........")
        Connector.find(p.side.map(_.side)).foreach { cnt =>
          println( s"  $cnt chr ${cnt.chr}" )
        }
        println("........")
        Connector.find(p.side).foreach { cnt =>
          println( s"  $cnt chr ${cnt.chr}" )
        }
      case _ => println("no")
    }
    
    
  }
  
  implicit class HVLinePaint( hvLine: HVLine ) {
    def draw( c: Canvas, chr:Char ):Unit = {
      if hvLine.horiz then
        ((hvLine.a.x min hvLine.b.x) to (hvLine.a.x max hvLine.b.x)).foreach { x =>
          c.put( x, hvLine.a.y, chr )
        }
      else if hvLine.vert then
        ((hvLine.a.y min hvLine.b.y) to (hvLine.a.y max hvLine.b.y)).foreach { y =>
          c.put( hvLine.a.x, y, chr )
        }
    }
    def draw( c:Canvas, ch:Connector ):Unit = draw(c, ch.chr)
    def draw( c:Canvas ):Unit = draw( c, hvLine.connector.map(_.chr).getOrElse('+') )
  }
  
  @Test
  def isect02():Unit = {
    val canvas1 = Canvas(30,15)
    lines.foreach { line =>
      line.draw(canvas1)
    }
    
    val l_cnt = lines.length
    for(
      i <- 0 until l_cnt;
      j <- i+1 until l_cnt
    ) lines(i).intersection(lines(j)) match {
      case Some(Intersection(p, side)) => {
        Connector.find(side).foreach( ct => {
          canvas1.put(p.x, p.y, ct.chr)
        })
      }
      case _ =>
    }
    
    println("-".repeat(30))
    println(canvas1)
    println("-".repeat(30))
  }
}
