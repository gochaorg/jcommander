package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.geom._
import xyz.cofe.term.common.Color

sealed trait Menu 
  extends Widget
  with VisibleProp
  with LocationRWProp
  with SizeRWProp
  with FillBackground
  with TextProperty
  with ForegroundColor
  with WidgetInput
  with FocusOwnerBgColor
  with FocusContainerBgColor
  with FocusOwnerFgColor
  with FocusContainerFgColor

class MenuContainer 
  extends Menu
  with WidgetChildren[Menu]
  with ForegroundColor
  with PaintText:
    def this(text:String) = {
      this()
      this.text.set(text)
    }

class MenuAction
  extends Menu:
    def this(text:String) = {
      this()
      this.text.set(text)
    }

class MenuBar 
  extends Widget
  with WidgetChildren[Menu]
  with VisibleProp
  with LocationRWProp
  with SizeRWProp
  with FillBackground
  with PaintChildren
  with WidgetInput:

  backgroundColor.set( Color.CyanBright )

  protected var rootListeners : List[ReleaseListener] = List.empty
  protected var rootWidget : Option[RootWidget] = None

  def install( rootWidget:RootWidget ):Unit =
    if rootWidget.children.exists(_.isInstanceOf[MenuBar])
    then throw Error("MenuBar already installed")

    rootWidget.children.append(this)
    rootListeners = rootWidget.children.onInsert( reinstallOnInsert ) :: rootListeners
    rootListeners = rootWidget.children.onDelete( reinstallOnDelete ) :: rootListeners

    this.rootWidget = Some(rootWidget)

    location.set( Position(0,0) )
    size.set( Size(rootWidget.size.get.width(),1) )
    rootListeners = rootWidget.size.onChange { resizeDelayed() } :: rootListeners

  def uninstall():Unit =
    rootListeners.foreach(_.release())
    rootListeners = List()
    rootWidget = None

  protected def reinstallOnInsert( wid:Widget ):Unit =
    if wid!=this
    then
      this.toTreePath.selfSibIndex match
        case Some(selfIdx) =>
          wid.toTreePath.selfSibIndex match
            case None => 
            case Some(widIdx) =>
              if selfIdx < widIdx then moveToFrontDelayed()
        case None => 

  protected def reinstallOnDelete( wid:Widget ):Unit =
    if wid==this 
    then uninstall()
    else moveToFrontDelayed()

  protected def moveToFrontDelayed():Unit = 
    rootWidget.map(_.session).foreach { ses => ses.addJob( moveToFront ) }

  protected def moveToFront():Unit =
    rootWidget.foreach { rootWid => 
      rootWid.children.delete( MenuBar.this )
      rootWid.children.append( MenuBar.this )
    }

  protected def resizeDelayed():Unit =
    rootWidget.map(_.session).foreach(_.addJob(resize))

  protected def resize():Unit =
    rootWidget.foreach { rwid => 
      size.set( Size(rwid.size.get.width(),1) )
    }

  children.onInsert { mu => 
    repositionChildren
  }

  def repositionChildren =
    children.foldLeft( None:Option[Rect] ){ case (prev,mi) => 
      prev match
        case None => 
          mi.size.set( Size(mi.text.get.length(), 1) )
          mi.location.set( Position(0,0) )
          Some( mi.size.get.leftUpRect(0,0) )          
        case Some(prevRect) =>
          mi.size.set( Size(mi.text.get.length(), 1) )
          mi.location.set( Position(prevRect.right+1,0) )
          Some( mi.size.get.leftUpRect(mi.location.get) )      
    }

  // paintStack.add(renderMenubar)
  // def renderMenubar( paintCtx:PaintCtx ):Unit = 
  //   ()

