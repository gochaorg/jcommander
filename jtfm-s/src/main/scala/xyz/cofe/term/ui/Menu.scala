package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.geom._
import xyz.cofe.term.common.Color
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputKeyEvent

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
  with FocusContainerFgColor:
    def keyMap: Map[KeyName,()=>Unit]
    def keyMap_=( map:Map[KeyName,()=>Unit] ):Unit
    def selectMenu:Unit

class MenuContainer 
  extends Menu
  with WidgetChildren[Menu]
  with PaintText
  with PaintChildren:
    def this(text:String) = {
      this()
      this.text.set(text)
    }
    var keyMap:Map[KeyName,()=>Unit] = Map.empty

    override def input(inputEvent: InputEvent): Boolean = 
      inputEvent match
        case ke: InputKeyEvent => 
          if !ke.isModifiersDown
          then 
            val action = keyMap.get(ke.getKey())
            action.map { a => a() ; true }.getOrElse(false)
          else false
        case _ => 
          false

    focus.onAccept { _ => showSubMenu }
    focus.onLost { _ => 
      if !focus.contains then hideSubMenu 
    }

    def selectMenu: Unit = 
      println("select")

    def showSubMenu:Unit =
      children.foldLeft(1){ case (y,mi) => 
        mi.location = Position(0,y)
        mi.size = Size(10,1)
        mi.visible = true
        y + 1
      }

    def hideSubMenu:Unit =
      children.foreach { mi => 
        mi.visible = false
      }

    children.onInsert { mi =>
      mi.visible = false
    }

class MenuAction
  extends Menu
  with PaintText:
    def this(text:String) = {
      this()
      this.text.set(text)
    }

    var keyMap:Map[KeyName,()=>Unit] = Map.empty

    def selectMenu: Unit = 
      println("select")

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

    location = Position(0,0)
    size = Size(rootWidget.size.get.width(),1)
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
      size = Size(rwid.size.get.width(),1)
    }

  children.onInsert { mu => repositionChildren }
  children.onDelete { mu => repositionChildren }

  def repositionChildren =
    children.foldLeft( None:Option[Rect] ){ case (prev,mi) => 
      prev match
        case None => 
          mi.size = Size(mi.text.length(), 1)
          mi.location = Position(0,0)
          Some( mi.size.leftUpRect(0,0) )          
        case Some(prevRect) =>
          mi.size = Size(mi.text.length(), 1)
          mi.location = Position(prevRect.right+1,0)
          Some( mi.size.leftUpRect(mi.location.get) )      
    }

    children.foldLeft( None:Option[Menu] ){ case (prevOpt,mi) => 
      mi.keyMap = mi.keyMap + ( KeyName.Enter -> (()=>{mi.selectMenu}) )
      mi match
        case mc: MenuContainer => 
          mc.keyMap = mc.keyMap + ( KeyName.Down -> (()=>{mc.selectMenu}) )
        case _ => ()

      prevOpt match
        case None => 
        case Some(prev) =>
          prev.keyMap = prev.keyMap + ( KeyName.Right  -> (()=>{mi.focus.request}) )
          prev.keyMap = prev.keyMap + ( KeyName.Tab    -> (()=>{mi.focus.request}) )
          mi.keyMap = mi.keyMap + ( KeyName.Left       -> (()=>{prev.focus.request}) )
          mi.keyMap = mi.keyMap + ( KeyName.ReverseTab -> (()=>{prev.focus.request}) )
      
      Some(mi)
    }

