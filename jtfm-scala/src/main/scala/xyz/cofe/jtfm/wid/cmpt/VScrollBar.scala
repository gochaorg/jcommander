package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.ev.OwnProperty
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.wid.BackgroundProperty
import xyz.cofe.jtfm.wid.ForegroundProperty
import xyz.cofe.jtfm.wid.OpaqueProperty
import xyz.cofe.jtfm.wid._
import xyz.cofe.jtfm.gr.Symbols
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.MouseAction
import xyz.cofe.jtfm.gr.Point
import java.lang.ref.WeakReference

/** Вертикальный скролл-бар */
class VScrollBar 
  extends Widget[VScrollBar]
    with BackgroundProperty[VScrollBar]
    with ForegroundProperty[VScrollBar]
    with OpaqueProperty[VScrollBar]
{
  private val arrowUp   = "\u2bc5"
  private val arrowDown = "\u2bc6"
  private val marker    = "\u25c6"

  val value:OwnProperty[Double,VScrollBar] = new OwnProperty(0,this)
  value.listen{(_,_,_) => { this.repaint() }}

  override def render( gr:TextGraphics ):Unit = {
    this.renderOpaque(gr)

    gr.setForegroundColor(foreground)
    gr.setBackgroundColor(background)

    gr.putString(0,0,arrowUp)
    gr.putString(0,rect.height-1,arrowDown)

    val free = rect.height - 2
    if( free==1 ){
      gr.putString(0,1,marker)
    }else if( free>1 ){
      (0 until free).foreach { y =>
        val s = Symbols.SingleThin.vert + ""
        gr.putString(0,1+y,s)
      }

      val vActual = (value.value max 0.0) min 1.0
      val y = ((free-1) * vActual).toInt
      gr.putString(0,1+y,marker)
    }
  }

  private var scrollUp:List[()=>Unit] = List()
  def onScrollUp( listener: =>Unit ):()=>Unit = {
    val ls:()=>Unit = ()=>{ listener }
    scrollUp = ls :: scrollUp
    val ref = WeakReference(ls)
    () => {
      val r = ref.get
      if( r!=null ){
        scrollUp = scrollUp.filterNot( l => l==r )
      }
    }
  }

  private var scrollDown:List[()=>Unit] = List()
  def onScrollDown( listener: =>Unit ):()=>Unit = {
    val ls:()=>Unit = ()=>{ listener }
    scrollDown = ls :: scrollUp
    val ref = WeakReference(ls)
    () => {
      val r = ref.get
      if( r!=null ){
        scrollDown = scrollDown.filterNot( l => l==r )
      }
    }
  }

  private var scrollTo:List[Double=>Unit] = List()
  def onScrollTo( listener:Double=>Unit ):()=>Unit = {
    scrollTo = listener :: scrollTo
    val ref = WeakReference(listener)
    () => {
      val r = ref.get
      if( r!=null ){
        scrollTo = scrollTo.filterNot( l => l==r )
      }
    }
  }


  protected def inputMouseAction( ma:MouseAction ):Boolean = {
    //println(s"VScrollBar.inputMouseAction")
    if( rect.height<2 ){
      false
    }else{
      val pt:Point = ma
      if( pt.y==0 ){
        //println(s"VScrollBar.inputMouseAction#up")
        scrollUp.foreach( ls => ls() )
        true
      }else{
        if( pt.y==rect.height-1 ){
          //println(s"VScrollBar.inputMouseAction#down")
          scrollDown.foreach( ls => ls() )
          true
        }else{
          if( rect.height>3 ){
            //println(s"VScrollBar.inputMouseAction#to")
            val tot = rect.height-2
            val yval = ((pt.y-1).toDouble)/tot.toDouble
            scrollTo.foreach( ls => ls(yval) )
            true
          }else{
            false
          }
        }
      }
    }
  }

  override def input( ks:KeyStroke ):Boolean = {
    //println(s"VScrollBar")
    ks match {
      case ma:MouseAction => inputMouseAction(ma)
      case _ => false
    }
  }
}
