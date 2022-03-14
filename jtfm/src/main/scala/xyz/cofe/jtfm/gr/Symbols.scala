package xyz.cofe.jtfm.gr

/*
       0 1 2 3 4  5 6 7 8 9 a b c d e f
U+250x ─ ━ │ ┃ ┄ ┅ ┆ ┇ ┈ ┉ ┊ ┋ ┌ ┍ ┎ ┏
U+251x ┐ ┑ ┒ ┓ └ ┕ ┖ ┗ ┘	┙	┚	┛	├	┝	┞	┟
U+252x ┠	┡	┢	┣	┤	┥	┦	┧	┨	┩	┪	┫	┬	┭	┮	┯
U+253x ┰	┱	┲	┳	┴	┵	┶	┷	┸	┹	┺	┻	┼	┽	┾	┿
U+254x ╀	╁	╂	╃	╄	╅	╆	╇	╈	╉	╊	╋	╌	╍	╎	╏
U+255x ═	║	╒	╓	╔	╕	╖	╗	╘	╙	╚	╛	╜	╝	╞	╟
U+256x ╠	╡	╢	╣	╤	╥	╦	╧	╨	╩	╪	╫	╬	╭	╮	╯
U+257x ╰	╱	╲	╳	╴	╵	╶	╷	╸	╹	╺	╻	╼	╽	╾	╿
*/
object Symbols {
  trait Border {
    lazy val leftTop: Char
    lazy val rightTop: Char
    lazy val leftBottom: Char
    lazy val rightBottom: Char
    lazy val vert: Char
    lazy val horz: Char
    lazy val cross: Char
    lazy val vert2right: Char
    lazy val vert2left: Char
    lazy val hort2down: Char
    lazy val hort2up: Char
  }
  
  object SingleThin extends Border :
    override lazy val leftTop: Char = '\u250C'
    override lazy val rightTop: Char = '\u2510'
    override lazy val leftBottom: Char = '\u2514'
    override lazy val rightBottom: Char = '\u2518'
    override lazy val vert: Char = '\u2502'
    override lazy val horz: Char = '\u2500'
    override lazy val cross: Char = '\u253C'
    override lazy val vert2right: Char = '\u251C'
    override lazy val vert2left: Char = '\u2524'
    override lazy val hort2down: Char = '\u252C'
    override lazy val hort2up: Char = '\u2534'
  
  object DoubleThin extends Border :
    override lazy val leftTop: Char = '\u2554'
    override lazy val rightTop: Char = '\u2557'
    override lazy val leftBottom: Char = '\u255A'
    override lazy val rightBottom: Char = '\u255D'
    override lazy val vert: Char = '\u2551'
    override lazy val horz: Char = '\u2550'
    override lazy val cross: Char = '\u256C'
    override lazy val vert2right: Char = '\u2560'
    override lazy val vert2left: Char = '\u2563'
    override lazy val hort2down: Char = '\u2566'
    override lazy val hort2up: Char = '\u2569'
  
  enum Style:
    case Single, Double
  
  enum Side:
    case Left, Right, Top, Bottom
  
  enum StyledSide(val side: Side, val style: Style):
    case LeftSingle extends StyledSide(Side.Left, Style.Single)
    case TopSingle extends StyledSide(Side.Top, Style.Single)
    case RightSingle extends StyledSide(Side.Right, Style.Single)
    case BottomSingle extends StyledSide(Side.Bottom, Style.Single)
    case LeftDouble extends StyledSide(Side.Left, Style.Double)
    case TopDouble extends StyledSide(Side.Top, Style.Double)
    case RightDouble extends StyledSide(Side.Right, Style.Double)
    case BottomDouble extends StyledSide(Side.Bottom, Style.Double)
  
  import StyledSide.*
  
  enum Connector(val sides: List[StyledSide], val chr: Char):
    case LeftTopSingle extends Connector(List(RightSingle, BottomSingle).sortBy(_.ordinal), SingleThin.leftTop)
    case RightTopSingle extends Connector(List(LeftSingle, BottomSingle).sortBy(_.ordinal), SingleThin.rightTop)
    case LeftBottomSingle extends Connector(List(RightSingle, TopSingle).sortBy(_.ordinal), SingleThin.leftBottom)
    case RightBottomSingle extends Connector(List(LeftSingle, TopSingle).sortBy(_.ordinal), SingleThin.rightBottom)
    case HorizntalSingle extends Connector(List(LeftSingle, RightSingle).sortBy(_.ordinal), SingleThin.horz)
    case VerticalSingle extends Connector(List(TopSingle, BottomSingle).sortBy(_.ordinal), SingleThin.vert)
    case CrossSingle extends Connector(List(LeftSingle, TopSingle, RightSingle, BottomSingle).sortBy(_.ordinal), SingleThin.cross)
    case HorzDownSingle extends Connector(List(LeftSingle, RightSingle, BottomSingle).sortBy(_.ordinal), SingleThin.hort2down)
    case HorzUpSingle extends Connector(List(LeftSingle, RightSingle, TopSingle).sortBy(_.ordinal), SingleThin.hort2up)
    case VertLeftSingle extends Connector(List(TopSingle, BottomSingle, LeftSingle).sortBy(_.ordinal), SingleThin.vert2left)
    case VertRightSingle extends Connector(List(TopSingle, BottomSingle, RightSingle).sortBy(_.ordinal), SingleThin.vert2right)
    
    case LeftTopDouble extends Connector(List(RightDouble, BottomDouble).sortBy(_.ordinal), DoubleThin.leftTop)
    case RightTopDouble extends Connector(List(LeftDouble, BottomDouble).sortBy(_.ordinal), DoubleThin.rightTop)
    case LeftBottomDouble extends Connector(List(RightDouble, TopDouble).sortBy(_.ordinal), DoubleThin.leftBottom)
    case RightBottomDouble extends Connector(List(LeftDouble, TopDouble).sortBy(_.ordinal), DoubleThin.rightBottom)
    case HorizntalDouble extends Connector(List(LeftDouble, RightDouble).sortBy(_.ordinal), DoubleThin.horz)
    case VerticalDouble extends Connector(List(TopDouble, BottomDouble).sortBy(_.ordinal), DoubleThin.vert)
    case CrossDouble extends Connector(List(LeftDouble, TopDouble, RightDouble, BottomDouble).sortBy(_.ordinal), DoubleThin.cross)
    case HorzDownDouble extends Connector(List(LeftDouble, RightDouble, BottomDouble).sortBy(_.ordinal), DoubleThin.hort2down)
    case HorzUpDouble extends Connector(List(LeftDouble, RightDouble, TopDouble).sortBy(_.ordinal), DoubleThin.hort2up)
    case VertLeftDouble extends Connector(List(TopDouble, BottomDouble, LeftDouble).sortBy(_.ordinal), DoubleThin.vert2left)
    case VertRightDouble extends Connector(List(TopDouble, BottomDouble, RightDouble).sortBy(_.ordinal), DoubleThin.vert2right)
    
    case LeftTopSD extends Connector(List(BottomSingle, RightDouble).sortBy(_.ordinal), '\u2552')
    case LeftTopDS extends Connector(List(BottomDouble, RightSingle).sortBy(_.ordinal), '\u2553')
    case RightTopSD extends Connector(List(BottomSingle, LeftDouble).sortBy(_.ordinal), '\u2555')
    case RightTopDS extends Connector(List(BottomDouble, LeftSingle).sortBy(_.ordinal), '\u2556')
    
    case LeftBottomSD extends Connector(List(TopSingle, RightDouble).sortBy(_.ordinal), '\u2558')
    case LeftBottomDS extends Connector(List(TopDouble, RightSingle).sortBy(_.ordinal), '\u2559')
    case RightBottomDS extends Connector(List(TopSingle, LeftDouble).sortBy(_.ordinal), '\u255B')
    case RightBottomSD extends Connector(List(TopDouble, LeftSingle).sortBy(_.ordinal), '\u255C')
    
    case VertRightSD extends Connector(List(TopSingle, BottomSingle, RightDouble).sortBy(_.ordinal), '\u255E')
    case VertRightDS extends Connector(List(TopDouble, BottomDouble, RightSingle).sortBy(_.ordinal), '\u255F')
    case VertLeftSD extends Connector(List(TopSingle, BottomSingle, LeftDouble).sortBy(_.ordinal), '\u2561')
    case VertLeftDS extends Connector(List(TopDouble, BottomDouble, LeftSingle).sortBy(_.ordinal), '\u2562')
    
    case HorzDownDS extends Connector(List(LeftDouble, RightDouble, BottomSingle).sortBy(_.ordinal), '\u2564')
    case HorzDownSD extends Connector(List(LeftSingle, RightSingle, BottomDouble).sortBy(_.ordinal), '\u2565')
    case HorzUpDS extends Connector(List(LeftDouble, RightDouble, TopSingle).sortBy(_.ordinal), '\u2567')
    case HorzUpSD extends Connector(List(LeftSingle, RightSingle, TopDouble).sortBy(_.ordinal), '\u2568')
    
    case HorzVertDS extends Connector(List(LeftDouble, RightDouble, TopSingle, BottomSingle).sortBy(_.ordinal), '\u256A')
    case HorzVertSD extends Connector(List(LeftSingle, RightSingle, TopDouble, BottomDouble).sortBy(_.ordinal), '\u256B')
  
  object Connector:
    implicit class SidesOps(val sides: Seq[Side]):
      def has(side: Side): Boolean = sides.find(_ == side).headOption.map(_ => true).getOrElse(false)
    
    implicit class StylesSidesOps(val sides: Seq[StyledSide]):
      def has(side: Side): Boolean = sides.find(_.side == side).headOption.map(_ => true).getOrElse(false)
    
    def find(sides: Seq[Side]): List[Connector] =
      var res = List[Connector]()
      val mustLeft: Boolean = sides.has(Side.Left)
      val mustRight: Boolean = sides.has(Side.Right)
      val mustTop: Boolean = sides.has(Side.Top)
      val mustBottom: Boolean = sides.has(Side.Bottom)
      Connector.values.foreach { cnt => {
        if cnt.sides.has(Side.Left) == mustLeft
          && cnt.sides.has(Side.Right) == mustRight
          && cnt.sides.has(Side.Top) == mustTop
          && cnt.sides.has(Side.Bottom) == mustBottom
        then
          res = cnt :: res
      }
      }
      res
    
    def find(sides: => Seq[StyledSide]): List[Connector] =
      val sides_sorted = sides.sortBy(_.ordinal)
      Connector.values.filter { c => c.sides == sides_sorted }.toList
}
