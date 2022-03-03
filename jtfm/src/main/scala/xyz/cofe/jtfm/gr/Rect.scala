package xyz.cofe.jtfm.gr

/**
 * Рамка с координатами (left,top,right,bottom)
 */
final case class Rect(left:Int, top:Int, right:Int, bottom:Int) {
    require( left<=right, "left more than right" )
    require( top<=bottom, "top more than bottom" )
    lazy val width:Int = right-left
    lazy val height:Int = bottom-top
    lazy val leftTop:Point = Point(left,top)
    lazy val rightTop:Point = Point(right,top)
    lazy val leftBottom:Point = Point(left,bottom)
    lazy val rightBottom:Point = Point(right,bottom)
    def include(
        x:Int, y:Int, 
        inc_left:Boolean, inc_right:Boolean, inc_top:Boolean, inc_bottom:Boolean 
    ):Boolean = {
        if( !( (inc_left && left <= x) || (!inc_left && left < x) ) ) {
            false
        }else if( !( (inc_right && x <= right) || (!inc_right && x < right) ) ) {
            false
        }else if( !( (inc_top && top <= y) || (!inc_top && top < y) ) ) {
            false
        } else if( !( (inc_bottom && y <= bottom) || (!inc_bottom && y < bottom) ) ) {
            false
        } else {
            true
        }
    }
    def include( x:Int, y:Int, inc_right:Boolean, inc_bottom:Boolean ):Boolean = 
        include( x,y,true,inc_right,true,inc_bottom )
    def include( x:Int, y:Int, inc_right_bottom:Boolean ):Boolean = 
        include( x,y,true,inc_right_bottom,true,inc_right_bottom )
    def include( x:Int, y:Int ):Boolean = 
        include( x,y,false )
    def include( p:Point ):Boolean = include(p.x, p.y)
}

object Rect {
    def apply( x0:Int, y0:Int, x1:Int, y1:Int ):Rect = 
        Rect( x0.min(x1),y0.min(y1),x1.max(x0),y1.max(y0) )

    def apply( a:Point, b:Point ):Rect =
        Rect( 
        a.x min b.x, a.y min b.y, 
        a.x max b.x, a.y max b.y 
        )
}