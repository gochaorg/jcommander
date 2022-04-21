package xyz.cofe.jtfm.gr

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.input.MouseAction

/**
 * Точка в прострастве
 */
final case class Point(x:Int, y:Int) {
    /**
     * Перенос точки
     */
    def translate(p:Point):Point = Point(x=x+p.x, y=y+p.y)

    /**
     * Перенос точки
     */
    def translate(off_x:Int, off_y:Int):Point = Point(off_x+x, off_y+y)

    /**
     * Инверсия точки
     */
    def invert(x_inv:Boolean, y_inv:Boolean):Point = Point(if(x_inv)-x else x, if(y_inv)-y else y)
    
    /**
     * Разница
     * @param p точка
     * @return разница
     */
    def diff( p:Point ):Point = Point( x-p.x, y-p.y )

    /**
     * Инверсия точки
     */
    def invert():Point = invert(true,true)

    /**
     * Возвращает текстовое представление
     * @return для x=5, y=3 вернет "(5;3)"
     */
    override def toString():String = s"($x;$y)"
}

object Point {
    def apply(str:String):Either[Point,String] = parse(str).left.map( _._1 )
    def apply(pos:TerminalPosition):Point = Point(pos.getColumn, pos.getRow)
    def apply(pos:(Int,Int)):Point = Point(pos._1, pos._2)
    implicit class TermPosOps(val term_pos:TerminalPosition) {
        def toPoint():Point = Point(term_pos.getColumn, term_pos.getRow)
    }
    implicit class PointTermPosOps(val p:Point) {
        def toTerminalPosition():TerminalPosition = new TerminalPosition(p.x, p.y)
    }
    private lazy val parse_pattern = java.util.regex.Pattern.compile("\\((?<x>\\d+);(?<y>\\d+)\\)")

    def parse(str:String, from:Int=0):Either[(Point,Int),String] = {
        if(str==null){
            Right("argument is null")
        }else if( from>=str.length || from<0 ){
            Right("argument from out of range")
        }else{
            val rest = str.substring(from)
            val m = parse_pattern.matcher(rest)
            if( m.matches ){
                val x = m.group("x").toInt
                val y = m.group("y").toInt
                val next_off = m.end+from
                Left((Point(x,y),next_off))
            }else{
                Right(s"'$str' not match (\\d+;\\d+)")
            }
        }
    }

    implicit def fromPointToTerminalPosition( p:Point ):TerminalPosition =
        new TerminalPosition(p.x, p.y)

    implicit def terminalPosition2Point( p:TerminalPosition ):Point =
        Point(p.getColumn, p.getRow)

    implicit def mouseAction2Point( ma:MouseAction ):Point =
        Point(ma.getPosition.getColumn, ma.getPosition.getRow)

    }
