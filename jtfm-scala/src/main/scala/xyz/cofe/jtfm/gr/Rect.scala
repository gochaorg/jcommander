package xyz.cofe.jtfm.gr

import com.googlecode.lanterna.graphics.TextGraphics

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
    lazy val size:Size2D = Size2D(width,height)

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

    //def move( p:Point ):Rect = Rect(leftTop.translate(p)).size(width,height)
    //def move( x:Int, y:Int ):Rect = Rect( x,y ).size( width, height )
    def translate( x:Int, y:Int ):Rect = Rect( left+x, top+y ).size( width, height )
    object reSize {
        def extend( w:Int, h:Int ):Rect = Rect(leftTop).size( (width+w) max 0, (height+h) max 0 )
        def extend( p:Point ):Rect = extend( p.x, p.y )
        def height_=( h:Int ):Rect = Rect(leftTop).size(width, h max 0)
        def setHeight( h:Int ):Rect = Rect(leftTop).size(width, h max 0)
        def width_=( w:Int ):Rect = Rect(leftTop).size(w max 0, height)
        def setWidth( w:Int ):Rect = Rect(leftTop).size(w max 0, height)
        def set( w:Int, h:Int ):Rect = Rect(leftTop).size( w max 0, h max 0 )
    }

    override def toString():String = s"Rect{ lt=($left,$top), rb=($right,$bottom) sz=$size}"
}

object Rect {
    def apply( x0:Int, y0:Int, x1:Int, y1:Int ):Rect = 
        new Rect( x0.min(x1),y0.min(y1),x1.max(x0),y1.max(y0) )

    def apply( a:Point, b:Point ):Rect =
        new Rect(
        a.x min b.x, a.y min b.y, 
        a.x max b.x, a.y max b.y 
        )

    def apply( a:Point ):FirstPoint = FirstPoint( a )
    def apply( x:Int, y:Int ):FirstPoint = FirstPoint( Point(x,y) )
    
    case class FirstPoint( val p:Point ){
        def size( width:Int, height:Int ):Rect = Rect( p, p.translate(width, height) )
        def size( s:Point ):Rect = Rect( p, p.translate(s) )
        def size( s:Size2D ):Rect = Rect( p, p.translate(s.width, s.height) )
        
        def to( x:Int, y:Int ):Rect = Rect( p, Point(x,y) )
        def to( to:Point ):Rect = Rect( p, to )
    }
    
    implicit class RectPaint( gr:TextGraphics ) {
        def fill( rect:Rect, ch:Char=' ' ):Unit = {
            if( rect.width>0 && rect.height>0 ){
                for(
                    y <- rect.top to rect.bottom;
                    x <- rect.left to rect.right
                ) gr.setCharacter(x,y,ch)
            }
        }
    }
}