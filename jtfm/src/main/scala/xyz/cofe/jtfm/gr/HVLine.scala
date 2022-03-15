package xyz.cofe.jtfm.gr

import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.gr.HVLine.{h_side_d, h_side_s, v_side_d, v_side_s}
import xyz.cofe.jtfm.gr.Symbols.{Connector, Style, StyledSide}

case class HVLine( a:Point, b:Point, style:Style ) {
  lazy val horiz:Boolean = (a diff b).y == 0
  lazy val vert:Boolean = (a diff b).x == 0
  def map[B]( f:HVLine => B )=f(this)
  
  def intersection( line: HVLine ):Option[Intersection] = {
    def pOrder : HVLine=>HVLine = l =>
      HVLine(
        Point( l.a.x min l.b.x, l.a.y min l.b.y ),
        Point( l.a.x max l.b.x, l.a.y max l.b.y ),
        l.style
      )
    
    if horiz && line.horiz then
      None
    else if vert && line.vert then
      None
    else
      val hline = (if horiz then this else line) map pOrder
      val vline = (if vert then this else line) map pOrder
      if hline.a.x <= vline.a.x && vline.a.x <= hline.b.x then
        if vline.a.y <= hline.a.y && hline.a.y <= vline.b.y then
          val p = Point(vline.a.x, hline.a.y)
          var sides = List[StyledSide]()
          
          if hline.a.x < p.x && p.x < hline.b.x then
            hline.style match {
              case Style.Single => sides = StyledSide.LeftSingle :: StyledSide.RightSingle :: sides
              case Style.Double => sides = StyledSide.LeftDouble :: StyledSide.RightDouble :: sides
            }
          else if hline.a.x < p.x && p.x == hline.b.x then
            hline.style match {
              case Style.Single => sides = StyledSide.LeftSingle :: sides
              case Style.Double => sides = StyledSide.LeftDouble :: sides
            }
          else if hline.a.x == p.x && p.x < hline.b.x then
            hline.style match {
              case Style.Single => sides = StyledSide.RightSingle :: sides
              case Style.Double => sides = StyledSide.RightDouble :: sides
            }
          
          if      vline.a.y <  p.y && p.y <  vline.b.y then
            vline.style match {
              case Style.Single => sides = StyledSide.TopSingle :: StyledSide.BottomSingle :: sides
              case Style.Double => sides = StyledSide.TopDouble :: StyledSide.BottomDouble :: sides
            }
          else if vline.a.y <  p.y && p.y == vline.b.y then
            vline.style match {
              case Style.Single => sides = StyledSide.TopSingle :: sides
              case Style.Double => sides = StyledSide.TopDouble :: sides
            }
          else if vline.a.y == p.y && p.y <  vline.b.y then
            vline.style match {
              case Style.Single => sides = StyledSide.BottomSingle :: sides
              case Style.Double => sides = StyledSide.BottomDouble :: sides
            }
          
          Some(Intersection(p,sides))
        else
          None
      else
        None
  }

  lazy val horzConnector: Option[Connector] = this.horiz match {
    case true  =>
      this.style match {
        case Style.Single => Connector.find(h_side_s).headOption
        case Style.Double => Connector.find(h_side_d).headOption
      }
    case false => None
  }
  lazy val vertConnector: Option[Connector] = this.vert match {
    case true  =>
      this.style match {
        case Style.Single => Connector.find(v_side_s).headOption
        case Style.Double => Connector.find(v_side_d).headOption
      }
    case false => None
  }
  lazy val connector: Option[Connector] = horzConnector.orElse(vertConnector)
}

object HVLine {
  protected val h_side_s = List(StyledSide.LeftSingle, StyledSide.RightSingle).sortBy(_.ordinal)
  private val h_side_d = List(StyledSide.LeftDouble, StyledSide.RightDouble).sortBy(_.ordinal)
  private val v_side_s = List(StyledSide.TopSingle, StyledSide.BottomSingle).sortBy(_.ordinal)
  private val v_side_d = List(StyledSide.TopDouble, StyledSide.BottomDouble).sortBy(_.ordinal)
  
  implicit class HVLineDraw( val hvLine: HVLine ) {
    def draw( gr:TextGraphics, chr:Char ):Unit = {
      if hvLine.horiz then
        ((hvLine.a.x min hvLine.b.x) to (hvLine.a.x max hvLine.b.x)).foreach { x =>
          gr.setCharacter(x, hvLine.a.y, chr )
        }
      else if hvLine.vert then
        ((hvLine.a.y min hvLine.b.y) to (hvLine.a.y max hvLine.b.y)).foreach { y =>
          gr.setCharacter( hvLine.a.x, y, chr )
        }
    }
    def draw( c:TextGraphics, ch:Connector ):Unit = draw(c, ch.chr)
    def draw( c:TextGraphics ):Unit = draw(c, hvLine.connector.map(_.chr).getOrElse('+') )
  }
  
  implicit class GridDraw( val lines:Seq[HVLine] ) {
    def draw( gr:TextGraphics ):Unit = {
      lines.foreach { line => line.draw(gr) }
      val l_cnt = lines.length
      for(
        i <- 0 until l_cnt;
        j <- i+1 until l_cnt
      ) lines(i).intersection(lines(j)) match {
        case Some(Intersection(p, side)) => {
          Connector.find(side).foreach( ct => {
            gr.setCharacter(p.x, p.y, ct.chr)
          })
        }
        case _ =>
      }
    }
  }
}