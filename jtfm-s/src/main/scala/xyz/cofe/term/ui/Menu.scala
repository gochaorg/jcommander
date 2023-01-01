package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.paint._
import xyz.cofe.term.geom._
import xyz.cofe.term.common.Color
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.lazyp.Prop

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

class MenuContainer 
  extends Menu
  with WidgetChildren[Menu]
  with PaintText:
    def this(text:String) = {
      this()
      this.text = text
      this.size = Size(text.length(),1)
    }

    paintStack.set(
      paintStack.get :+ { paint => 
        paintChildren(paint)
      }
    )

    def paintChildren(paint:PaintCtx):Unit =
      children.get.foreach { widget => 
        def paintChild():Unit = {
          val loc  = widget.location.get
          val size = widget.size.get
          if size.height()>0 && size.width()>0
          then
            val wCtx = paint.context.offset(loc).size(size).clipping(false).build
            widget.paint(wCtx)
        }
        widget match
          case visProp:VisibleProp if visProp.visible.value.get => paintChild()
          case _ => ()
      }

    var keyMap:Map[KeyName,()=>Unit] = Map.empty

    focus.onAccept { _ => showSubMenu }
    focus.onLost { _ => checkHideSubMenu }

    def checkHideSubMenu:Unit =
      parent.get.foreach {
        case mc:MenuContainer => mc.checkHideSubMenu
        case _ =>
      }

      if !focus.contains 
      then 
        hideSubMenu 

    def selectMenu: Unit = 
      children.headOption.foreach { _.focus.request }

    def showSubMenu:Unit =
      upDownLayout
      bindUpDown

    private def childsMaxWidth = { 
      children.map(_.text.length()).maxOption.getOrElse(5) 
    }
    private lazy val childsVisibleOffset = Prop.rw(0)
    private lazy val childsVisibleCountMax = Prop.rw(10) 
    private lazy val childsCount = Prop.eval(children) { _.size }
    private lazy val childsVisibleOffsetMax = 
      Prop.eval(childsCount, childsVisibleCountMax) { 
        case (cnt,cmax) => 0 max (cnt - cmax)
    }
    private lazy val childsVisibleItems = 
      Prop.eval( children, childsVisibleOffset, childsVisibleCountMax ) {
        case (childs, off, cmax) =>
          val off2 = off + cmax
          children.zipWithIndex
            .map { case (itm,idx) => (itm,idx>=off && idx<off2) }
            .flatMap { case (itm,b) => b match 
              case true => List(itm)
              case false => List.empty
            }
      }

    childsVisibleOffset.onChange( Session.addJob(upDownLayout) )
    childsVisibleCountMax.onChange( Session.addJob(upDownLayout) )

    paintStack.set(
      paintStack.get :+ { paint => 
      if focus.contains then renderBorder(paint)
    })

    def renderBorder(paint:PaintCtx):Unit = {
      val contentHeight = childsVisibleItems.get.size
      val contentWidth = childsMaxWidth
      val lt = Position(0,1)
      val rb = Position(lt.x+1+contentWidth, lt.y+1+contentHeight)
      val rt = Position(rb.x, lt.y)
      val lb = Position(lt.x, rb.y)

      val style = 
        if focus.isOwner || children.exists(_.focus.isOwner)
        then Symbols.Style.Double else Symbols.Style.Single 
      val lines = List(
        Line(lt,rt,style),
        Line(rt,rb,style),
        Line(lb,rb,style),
        Line(lt,lb,style),
      )
      val hvLines = lines.flatMap { line => line.toHVLine() match
        case None => List()
        case Some(value) => List(value)
      }
      paint.foreground = paintTextColor
      paint.background = fillBackgroundColor
      hvLines.draw(paint)
    }

    def upDownLayout: Unit =
      val off = childsVisibleOffset.get
      val cmax = childsVisibleCountMax.get
      val off2 = off + cmax
      children.zipWithIndex.foreach { case (mi,idx) => 
        mi.location = Position(1,2+idx-childsVisibleOffset.get)
        mi.size = Size( childsMaxWidth, 1 )
        mi.visible = idx>=off && idx<off2
      }

    def bindUpDown:Unit =      
      children.zip(children.drop(1)).foreach { case (prevMi,nextMi) => 
        prevMi.keyMap = prevMi.keyMap + ( KeyName.Down -> ( ()=>{focusChildNext(nextMi)} ) )
        prevMi.keyMap = prevMi.keyMap + ( KeyName.Tab ->  ( ()=>{focusChildNext(nextMi)} ) )

        nextMi.keyMap = nextMi.keyMap + ( KeyName.Up         -> ( ()=>{focusChildPrev(prevMi)} ) )
        nextMi.keyMap = nextMi.keyMap + ( KeyName.ReverseTab -> ( ()=>{focusChildPrev(prevMi)} ) )
      }
      children.headOption.foreach { wid => wid.keyMap = wid.keyMap + ( KeyName.Up -> (()=>{MenuContainer.this.focus.request}) ) }
      children.foreach { mi =>
        mi.keyMap = mi.keyMap + ( KeyName.Enter -> (()=>{mi.selectMenu}) )
      }
      
    private def focusChildNext( mi:Menu ):Unit =
      if !(mi.visible:Boolean) then
        childsVisibleOffset.set( (childsVisibleOffset.get+1) min childsVisibleOffsetMax.get )
        mi.visible = true
      mi.focus.request

    private def focusChildPrev( mi:Menu ):Unit =
      if !(mi.visible:Boolean) then
        childsVisibleOffset.set( (childsVisibleOffset.get-1) max 0 )
        mi.visible = true
      mi.focus.request

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
      this.text = text
      this.size = Size(text.length(),1)
    }

    focus.onLost { _ => checkHideSubMenu }
    def checkHideSubMenu:Unit =
      parent.get.foreach {
        case mc:MenuContainer => mc.checkHideSubMenu
        case _ =>
      }

    var keyMap:Map[KeyName,()=>Unit] = Map.empty

    def selectMenu: Unit = 
      onActionListener.foreach { ls => ls() }

    var onActionListener : List[()=>Unit] = List.empty
    def onAction( listener: => Unit ):ReleaseListener =
      val ls:()=>Unit = ()=>listener
      onActionListener = ls :: onActionListener
      new ReleaseListener {
        def release(): Unit = 
          onActionListener = onActionListener.filterNot( l => l==ls )
      }

    def action( ls: => Unit ):this.type =
      onAction(ls)
      this

class MenuBar 
  extends Widget
  with WidgetChildren[Menu]
  with VisibleProp
  with LocationRWProp
  with SizeRWProp
  with FillBackground
  //with PaintChildren
  with WidgetInput:

  paintStack.set(
    paintStack.get :+ { paint => 
      paintChildren(paint)
    }
  )

  def paintChildren(paint:PaintCtx):Unit =
    children.get.foreach { widget => 
      def paintChild():Unit = {
        val loc  = widget.location.get
        val size = widget.size.get
        if size.height()>0 && size.width()>0
        then
          val wCtx = paint.context.offset(loc).size(size).clipping(false).build
          widget.paint(wCtx)
      }
      widget match
        case visProp:VisibleProp if visProp.visible.value.get => paintChild()
        case _ => ()
    }

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

