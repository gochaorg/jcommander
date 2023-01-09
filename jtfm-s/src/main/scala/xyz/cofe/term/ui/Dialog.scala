package xyz.cofe.term.ui

import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.paint._
import xyz.cofe.term.geom._
import xyz.cofe.term.common.Position
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.common.Size

class Dialog
extends Widget 
with VisibleProp
with LocationRWProp
with SizeRWProp
with FillBackground
with PaintTextColor
with TextProperty
with WidgetChildren[Widget]
with PaintChildrenMethod
with WidgetInput:
  paintStack.add(paintBorder)
  paintStack.add(paintTitle)
  paintStack.add(paintChildren)

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
    lines.draw(paint)

  def paintTitle(paint:PaintCtx):Unit =
    val title = text.get.take(size.get.width()-2)
    paint.foreground = paintTextColor
    paint.background = fillBackgroundColor
    paint.write(1,1,title)

  val closeButton = Button(""+Symbols.Action.Close)
  children.append(closeButton)
  closeButton.onAction {
    hide()
  }

  var focusableWidget:Option[WidgetInput] = None
  private def findeFocusableChild:Option[WidgetInput] =
    this.walk.path.find { p => p.node.isInstanceOf[WidgetInput] }.map(_.node.asInstanceOf[WidgetInput])

  size = Size(20,5)

  def show(pos:Position)=
    visible = true
    add2root { root =>
      move2frontOf(root)
      location = pos
      focusableWidget.orElse(findeFocusableChild).foreach { _.focus.request }
    }

  def show() = 
    visible = true
    add2root { root =>
      move2frontOf(root)
      move2centerOf(root)
      focusableWidget.orElse(findeFocusableChild).foreach { _.focus.request }
    }

  def hide() =
    visible = false
    parent.get.foreach { prnt =>
      prnt.children.delete(this)
    }

  private var onShowEmitted = false
  val onShowed = Listener()

  private var onHideEmitted = false
  val onHided = Listener()

  paintStack.add { _ => 
    if ! onShowEmitted then
      onShowed.emit()
      onShowEmitted = true
      onHideEmitted = false
  }

  parent.onChange {
    Session.addJob {
      if parent.get.isEmpty then
        if ! onHideEmitted then
          onShowEmitted = false
          onHideEmitted = true
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

  case class Builder(
    configure:List[Dialog=>Unit]=List.empty,
    location:Option[Position]=None
  ):
    def onHide( code: =>Unit )=
      copy( configure = configure :+ (_.onHided(code)) )

    def onShow( code: =>Unit )=
      copy( configure = configure :+ (_.onShowed(code)) )

    def content( init: Dialog=>Unit )=
      copy( configure = configure :+ (dlg => init(dlg)) )

    def size( size:Size )=
      copy( configure = configure :+ (dlg => dlg.size = size) )

    def location( pos:Position )=
      copy( location = Some(pos) )

    def show() =
      val dlg = Dialog()
      configure.foreach(_(dlg))
      if location.isDefined then
        dlg.show(location.get)
      else
        dlg.show()
      dlg
