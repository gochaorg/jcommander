package xyz.cofe.jtfm.gr

import xyz.cofe.jtfm.gr.Symbols.{Style, StyledSide}

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
}
