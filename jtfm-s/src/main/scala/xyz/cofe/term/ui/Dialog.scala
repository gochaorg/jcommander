package xyz.cofe.term
package ui

import xyz.cofe.term.ui.prop._
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.paint._
import xyz.cofe.term.geom._
import xyz.cofe.term.common.Position
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.common.Size
import xyz.cofe.term.ui.paint._
import xyz.cofe.term.common.Color
import conf._
import xyz.cofe.term.ui.ses.conf.DialogConf
import xyz.cofe.term.cs._
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import xyz.cofe.log._

class Dialog( using conf:DialogConf )
extends Widget 
with LocationRWProp
with SizeRWProp
with FillBackground
with PaintTextColor
with TextProperty
with WidgetChildren[Widget]
with PaintChildrenMethod
with WidgetInput:
  private implicit val logger: Logger = LoggerFactory.getLogger("xyz.cofe.term.ui.Dialog")

  paintStack.add(paintBorder)
  paintStack.add(paintTitle)
  paintStack.add(paintChildren)

  backgroundColor = Color.White
  foregroundColor = Color.Black

  def paintBorder(paint:PaintCtx):Unit = 
    val rect = size.get.leftUpRect(0,0)
    val lt = rect.leftTop
    val rt = rect.rightTop
    val lb = rect.leftBottom
    val rb = rect.rightBottom
    val hlLeft = lt.move(0,2)
    val hlRight = rt.move(0,2)
    val lines = List(
      Line(lt,rt,Symbols.Style.Double),
      Line(lb,rb,Symbols.Style.Single),
      Line(lt,lb,Symbols.Style.Single),
      Line(rt,rb,Symbols.Style.Single),
      Line(hlLeft,hlRight,Symbols.Style.Single),
    )
    paint.foreground = Color.Black
    paint.background = Color.White
    lines.draw(paint)

  def paintTitle(paint:PaintCtx):Unit =
    val title = text.get.take(size.get.width()-2)
    paint.foreground = paintTextColor
    paint.background = fillBackgroundColor
    paint.write(1,1,title)

  val closeButton = Button(""+Symbols.Action.Close)
  children.append(closeButton)
  closeButton.onAction.listen(close())

  val content = Panel()
  content.backgroundColor = Color.Green
  children.append(content)
  content.bind(this){ w =>     
    Rect(1,3, (w.width-1) max 0, (w.height-3) max 0)
  }

  var focusableWidget:Option[WidgetInput] = None
  private def findeFocusableChild:Option[WidgetInput] =
    this.walk.path.find { p => p.node.isInstanceOf[WidgetInput] }.map(_.node.asInstanceOf[WidgetInput])

  size = Size(20,5)

  def open(pos:Position):Unit=
    visible = true
    add2root { root =>
      move2frontOf(root)
      location = pos
      focusableWidget.orElse(findeFocusableChild).foreach { _.focus.request }
    }

  def open():Unit = 
    visible = true
    add2root { root =>
      move2frontOf(root)
      move2centerOf(root)
      focusableWidget.orElse(findeFocusableChild).foreach { _.focus.request }
    }

  def close():Unit =
    log"close"
    visible = false
    parent.get.foreach { prnt =>
      prnt match
        case wc:WidgetChildren[?] =>
          wc.children.delete(this)
        case _ =>
    }
    onClosed.emit()
    if conf.restoreFocusAtClose then restoreFocus()

  private var lastFocused : Option[WidgetInput] = None
  focus.onAccept.listen { fromOpt => 
    debug"accepted focus from $fromOpt"
    acceptFocusFrom( fromOpt )
  }

  focus.acceptChild.listen { case (fromOpt,childFocus) => 
    debug"accepted 1 focus from $fromOpt"
    acceptFocusFrom( fromOpt )
  }

  private def acceptFocusFrom( fromOpt:Option[WidgetInput] ):Unit =
    if fromOpt.map { from => ! from.toTreePath.listToLeaf.contains(this) }.getOrElse(false) then
      debug"acceptFocusFrom not subchild"
      lastFocused = fromOpt

  private def restoreFocus():Unit =
    log"restore focus to $lastFocused"
    lastFocused.foreach { _.focus.request }
    lastFocused = None

  private var onOpenEmitted = false
  val onOpenned = Listener.unit

  private var onCloseEmitted = false
  val onClosed = Listener.unit

  paintStack.add { _ => 
    if ! onOpenEmitted then
      debug"open emitting"
      onOpenned.emit()
      onOpenEmitted = true
      onCloseEmitted = false
  }

  parent.onChange {
    Session.addJob {
      if parent.get.isEmpty then
        if ! onCloseEmitted then
          onOpenEmitted = false
          onCloseEmitted = true
    }
  }

  private def add2root(after:RootWidget => Unit) =
    val self = this
    Session.addJob {
      Session.currentSession.foreach { ses => 
        if !ses.rootWidget.children.contains(self) then
          ses.rootWidget.children.append(self)
          after(ses.rootWidget)
        else
          after(ses.rootWidget)
      }
    }

  private def move2centerOf(root:RootWidget) =
    location = root.locationRect.get.center.move( - size.get.width()/2, - size.get.height()/2 )

  private def move2frontOf(root:RootWidget) =
    val self = this
    toTreePath.selfSibIndex.foreach { idx => 
      if idx < (root.children.size-1) then
        root.children.delete(self)
        root.children.append(self)
    }

object Dialog:
  def title(string:String) = 
    Builder().copy(
      configure = List( { dlg => dlg.text = string } )
    )

  trait Handler:
    def onOpen( ls: => Any ):Unit
    def close():Unit 

  class HandlerImpl() extends Handler:
    private implicit val logger: Logger = LoggerFactory.getLogger("xyz.cofe.term.ui.Dialog.HandlerImpl")

    var dialog:Option[Dialog] = None
    def setDialog( dlg:Dialog ):Unit =
      debug"setDialog $dlg"
      dialog = Some(dlg)

    var onOpenListeners: List[()=>Any] = List.empty
    def onOpen( ls: => Any ):Unit = {
      debug"add onOpen"
      onOpenListeners = ( ()=>{ 
        ls 
      }) :: onOpenListeners
    }

    def close():Unit = {
      dialog.foreach(_.close())
    }

  case class Builder(
    configure:List[Dialog=>Unit]=List.empty,
    location:Option[Position]=None,
    handler: HandlerImpl = new HandlerImpl
  ):
    private implicit val logger: Logger = LoggerFactory.getLogger("xyz.cofe.term.ui.Dialog.Builder")

    def onClose( code: =>Unit ):Builder =
      copy( configure = configure :+ (_.onClosed.listen(_ => code)) )

    def onOpen( code: =>Unit ):Builder =
      copy( configure = configure :+ (_.onOpenned.listen(_ => code)) )

    def content( init: Panel=>Unit ):Builder =
      copy( configure = configure :+ (dlg => init(dlg.content)) )

    def content( init: (Panel,Handler)=>Unit ):Builder =
      val r = copy( configure = configure :+ (dlg => init(dlg.content, handler)) )
      debug"onOpenListeners size ${r.handler.onOpenListeners.size}"
      r

    def size( size:Size ):Builder =
      copy( configure = configure :+ (dlg => dlg.size = size) )

    def size(width:Int, height:Int):Builder =
      size(Size(width,height))

    def location( pos:Position ):Builder =
      copy( location = Some(pos) )

    def open():Dialog =
      val dlg = Dialog()
      handler.setDialog(dlg)
      configure.foreach(_(dlg))

      handler.onOpenListeners.foreach( ls => 
        debug"add close listener"
        dlg.onOpenned.listen { _ =>
          debug"call close listeners"
          ls()
        }
      )
      
      if location.isDefined then
        dlg.open(location.get)
      else
        dlg.open()
      dlg
