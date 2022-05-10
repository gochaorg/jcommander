package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.wid.BackgroundProperty
import xyz.cofe.jtfm.wid.ForegroundProperty
import xyz.cofe.jtfm.wid.OpaqueProperty
import xyz.cofe.jtfm.wid.TextProperty
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.gr.Align
import xyz.cofe.jtfm.ev.OwnProperty
import com.googlecode.lanterna.input.MouseAction
import xyz.cofe.jtfm.wid.MouseButton
import com.googlecode.lanterna.input.KeyStroke
import javax.swing.text.TabExpander
import xyz.cofe.jtfm.gr.TextBlock
import xyz.cofe.jtfm.gr.TabExpand

class Label
  extends Widget[Label]
    with BackgroundProperty[Label]
    with ForegroundProperty[Label]
    with OpaqueProperty[Label]
    with TextProperty[Label]:

  private var mouseListeners:List[MouseAction=>Boolean] = List()
  def onLeftClick( call: => Boolean ):()=>Unit = {
    val ls:MouseAction=>Boolean = ma=>{ 
      import xyz.cofe.jtfm.wid.MouseActionOps
      ma.button match {
        case Some(MouseButton.Left) if ma.isMouseDown =>
          call
        case _ =>
          false
      }
    }
    mouseListeners = ls :: mouseListeners
    ()=>{
      mouseListeners = mouseListeners.filterNot(_==ls)
    }
  }

  val tabExpand:OwnProperty[TabExpand,Label] = OwnProperty(TabExpand.FourAligned,this).observe( (_,_,_) => repaint() )._1
  val halign:OwnProperty[Align,Label] = OwnProperty(Align.Begin,this).observe((_,_,_)=>repaint())._1

  override def render(gr:TextGraphics):Unit=
    import xyz.cofe.jtfm.gr.TextGraphicsOps
    this.renderOpaque(gr)
    val txt = text.value
    if txt.length>0 then
      gr.setForegroundColor(foreground.value)
      gr.setBackgroundColor(background.value)
      val tb = halign.value match {
        case Align.Begin  => TextBlock(txt,tabExpand.value)
        case Align.Center => TextBlock(txt,tabExpand.value).cropRight(rect.width).alignCenter(rect.width)
        case Align.End    => TextBlock(txt,tabExpand.value).alignRight.cropLeft(rect.width)
      }
      (0 until tb.height).zip( tb.lines ).foreach { case(y,tline) =>
        gr.putString(tline.offset,y, tline.line)
      }

  override def input(ks:KeyStroke):Boolean = {
    ks match {
      case ma:MouseAction =>
        mouseListeners.map( ls => ls(ma) ).foldLeft(true)((a,b)=>a || b)
      case _ => false
    }
  }

