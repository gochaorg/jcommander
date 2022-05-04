package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.wid.BackgroundProperty
import xyz.cofe.jtfm.wid.ForegroundProperty
import xyz.cofe.jtfm.wid.OpaqueProperty
import xyz.cofe.jtfm.wid.FocusProperty
import xyz.cofe.jtfm.ev.OwnProperty
import scala.ref.WeakReference
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.gr.Symbols
import xyz.cofe.jtfm.gr.TextGraphicsOps
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.gr.Align
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.KeyStroke
import xyz.cofe.jtfm.gr.Point
import xyz.cofe.jtfm.wid.MouseActionOps
import xyz.cofe.jtfm.wid.MouseButton
import xyz.cofe.jtfm.wid.wc.Jobs
import xyz.cofe.jtfm.wid.WidgetCycle

class Dialog 
  extends Widget[Dialog] 
    with BackgroundProperty[Dialog]
    with ForegroundProperty[Dialog]
    with OpaqueProperty[Dialog]
    with FocusProperty[Dialog](true,10)
{
  self: Dialog =>

  lazy val title:OwnProperty[String,Dialog] =
    OwnProperty("",self.asInstanceOf[Dialog])
      .observe( (prop,old,cur)=>{ repaint() })._1

  private var closeListeners0 = List[Dialog=>Unit]()
  def closeListeners:List[Dialog=>Unit] = closeListeners0
  def onClose( action: Dialog=>Unit ):()=>Unit = {
    closeListeners0 = action :: closeListeners0
    val ref = WeakReference(action)
    ()=>{
      ref.get match {
        case Some(r) => 
          closeListeners0 = closeListeners0.filterNot(_==r)
          ref.clear
        case None =>
      }
    }
  }
  def onClose( action: =>Unit ):()=>Unit = {
    val ls:Dialog=>Unit = (_)=>{action}
    closeListeners0 = ls :: closeListeners0
    val ref = WeakReference(ls)
    ()=>{
      ref.get match {
        case Some(r) => 
          closeListeners0 = closeListeners0.filterNot(_==r)
          ref.clear
        case None =>
      }
    }
  }

  override def render(gr:TextGraphics):Unit = {
    this.renderOpaque(gr)
    if( rect.width>0 && rect.height>0 ){
      val lt = Point(0,0)
      val rt = Point(rect.width-1,0)
      val lb = Point(0,rect.height-1)
      val rb = Point(rect.width-1,rect.height-1)
      gr.drawLine( lt, rt, Symbols.SingleThin.horz )
      gr.drawLine( lb, rb, Symbols.SingleThin.horz )
      gr.drawLine( lt, lb, Symbols.SingleThin.vert )
      gr.drawLine( rt, rb, Symbols.SingleThin.vert )

      gr.draw(lt, Symbols.Round.leftTop)
      gr.draw(rt, Symbols.Round.rightTop)
      gr.draw(lb, Symbols.Round.leftBottom)
      gr.draw(rb, Symbols.Round.rightBottom)
    }

    val title = this.title.value
    if( title!=null && title.length>0 ){
      val w = rect.width-3
      if( w>0 ){
        gr.draw( Rect(1,0).size(w,1), title, Align.Center )
      }
    }

    gr.draw( closeButtonPoint, Symbols.Action.Close )
  }

  private def closeButtonPoint:Point = Point(rect.width-2,0)

  def close():Unit = {
    parent.value match {
      case Some(prnt) =>
        prnt.nested.remove(this)
      case _ =>
    }
    visible.value = false
    focus.history.lastGainNonChild.foreach { wid => if(wid.isInstanceOf[FocusProperty[_]]){
      wid.asInstanceOf[FocusProperty[_]].focus.request()      
    }};
    repaint()
    closeListeners0.foreach { ls => ls(this) }
  }

  protected def inputMouseAction(ma:MouseAction):Boolean = {
    val p:Point = ma.getPosition
    ma.button match {
      case Some(MouseButton.Left) if p==closeButtonPoint =>
        close()
        true
      case _ => false
    }
  }

  override def input(ks:KeyStroke):Boolean = {
    ks match {
      case ma:MouseAction => inputMouseAction(ma)
      case _ => false
    }
  }
}

object Dialog {
  def show( dlg:Dialog )( onClose: => Unit ):Unit = {
    Jobs.add {
      WidgetCycle.tryGet.foreach { wc =>
        wc.workState.foreach { ws => 
          ws.root.widgetTree.find( w => w==dlg ) match {
            case Some(_) =>
              dlg.parent.value match {
                case Some(prnt) =>
                  prnt.nested.remove(dlg)
                case _ =>
              }
            case _ =>
          }
          ws.root.nested.append(dlg)
          dlg.visible.value = true
          dlg.focus.request()
          val detach = dlg.onClose { onClose }
          dlg.visible.listen { (_,_,visible) => {
            if( !visible ){
              detach()
            }
          }}
        }
      }
    }
  }
}
