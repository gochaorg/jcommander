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
import com.googlecode.lanterna.TextColor

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
      gr.putString( 1,0, if( title.length>w )title.substring(0,w) else title )
    }

    gr.draw( closeButtonPoint, Symbols.Action.Close )
  }

  private def closeButtonPoint:Point = Point(rect.width-2,0)

  def close():Unit = {
    closeListeners0.foreach { ls => ls(this) }
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
  def createAndShow( dlg:Dialog )( onClose: => Unit )( postInit: =>Unit ):Unit = {
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
          postInit
        }
      }
    }
  }

  def show( build:DialogBuild ?=> Unit ):Unit = {
    import xyz.cofe.jtfm.wid.RectProperty._
    given dbuild:DialogBuild = DialogBuild()
    val dlg = Dialog()
    build
    dbuild.prepare.foreach( _(dlg) )
    dbuild.content.foreach { cnt =>
      dlg.nested.append(cnt)
      cnt.rect.value = Rect(1,1).size(cnt.rect.width-2, cnt.rect.height-2)
      cnt.rect.bindTo(dlg){ rct => Rect(1,1).size(rct.width-2, rct.height-2) }
    }
    createAndShow( dlg )({})({
      dbuild.postInit.foreach { _(dlg) }
    })
  }

  case class DialogBuild( 
    var prepare:List[Dialog=>Unit] = List(),
    var postInit:List[Dialog=>Unit] = List(),
    var content:Option[Widget[_]] = None
  )

  def prepare( build:Dialog=>Unit )(using dbuild:DialogBuild ):Unit =
    dbuild.prepare = dbuild.prepare :+ build

  def post( build:Dialog=>Unit )(using dbuild:DialogBuild ):Unit =
    dbuild.postInit = dbuild.postInit :+ build

  def content( wid:Widget[_] )(using dbuild:DialogBuild ):Unit =
    dbuild.content = Some(wid)

  def title( str:String )(using dbuild:DialogBuild ):Unit =
    prepare { dlg => dlg.title.value = str }
    
  def onClose( ls: => Unit )(using dbuild:DialogBuild ):Unit =
    prepare { dlg => dlg.onClose(ls) }
    
  def location( rect:Rect )(using dbuild:DialogBuild ):Unit =
    prepare { dlg => dlg.rect.value = rect }

  def size( w:Int, h:Int )(using dbuild:DialogBuild ):Unit =
    prepare { dlg => dlg.rect.value = Rect(dlg.rect.leftTop).size(w,h) }
    
  def toCenter()(using dbuild:DialogBuild ):Unit =
    post { dlg => 
      Jobs.add { 
        for { 
          wc <- WidgetCycle.tryGet
          ws <- wc.workState
        } {          
          val rootSize = ws.root.rect.value.size
          val dlgSize = dlg.rect.value.size
          val marginHor = rootSize.width - dlgSize.width
          val marginVer = rootSize.height - dlgSize.height
          val x = marginHor / 2
          val y = marginVer / 2
          dlg.rect.value = Rect(x,y).size(dlgSize)
        }
      }
    }

  case class ColorSet()

  def color( init: ColorSet ?=> Unit )(using dbuild:DialogBuild ):Unit =
    given cs:ColorSet = ColorSet()
    init

  def foreground( color:TextColor )(using dbuild:DialogBuild, cs:ColorSet ):Unit =
    prepare { dlg => dlg.foreground.value = color }

  def background( color:TextColor )(using dbuild:DialogBuild, cs:ColorSet ):Unit =
    prepare { dlg => dlg.background.value = color }
}
