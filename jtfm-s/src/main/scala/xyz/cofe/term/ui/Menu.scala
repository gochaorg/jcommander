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
import xyz.cofe.term.common.InputMouseButtonEvent
import xyz.cofe.term.common.MouseButton
import xyz.cofe.term.buff.ScreenChar
import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.ui.conf.MenuColorConfig
import xyz.cofe.term.ui.conf.MenuBarColorConfig
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.ui.prop.color._
import xyz.cofe.term.ui.paint._
import xyz.cofe.term.buff._

import org.slf4j.LoggerFactory
import org.slf4j.Logger
import xyz.cofe.log._

sealed trait Menu 
  extends Widget
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
        case me: InputMouseButtonEvent =>
          if me.button() == MouseButton.Left & me.pressed()
          then
            selectMenu
            true
          else
            false
        case _ => 
          false

    def renderText:String

/* #region MenuContainer */
class MenuContainer(using config: MenuColorConfig)
  extends Menu
  with WidgetChildren[Menu]:
    private implicit lazy val logger : Logger = LoggerFactory.getLogger("xyz.cofe.term.ui.MenuContainer")

    paintStack.add { _ => contentWidthComputed.set(false) }

    def this(text:String)(using config: MenuColorConfig) = {
      this()
      this.text = text
      this.size = Size(text.length(),1)
    }

    foregroundColor = config.foregroundColor
    backgroundColor = config.backgroundColor
    focusOwnerFgColor = config.focusOwnerFgColor
    focusOwnerBgColor = config.focusOwnerBgColor
    focusContainerFgColor = config.focusContainerFgColor
    focusContainerBgColor = config.focusContainerBgColor

    def renderText: String = 
      text.get + { 
        if menuLevel.get > 1
        then if children.nonEmpty 
          then "  "+Symbols.Trinagles.right 
          else "  " 
        else 
          ""
      }

    /* #region paint text */

    paintStack.set(
      paintStack.get :+ { paint =>
        paintText(paint)
      }
    )

    def paintTextColor:Color =
      if this.isInstanceOf[WidgetInput]
      then
        val foc = this.asInstanceOf[WidgetInput].focus
        if this.isInstanceOf[FocusOwnerFgColor] && foc.isOwner 
        then this.asInstanceOf[FocusOwnerFgColor].focusOwnerFgColor.get 
        else 
          if this.isInstanceOf[FocusContainerFgColor] && foc.contains 
          then this.asInstanceOf[FocusContainerFgColor].focusContainerFgColor.get 
          else foregroundColor.get
      else
        foregroundColor.get
    
    def paintText( paint:PaintCtx ):Unit =
      paint.foreground = paintTextColor

      if this.isInstanceOf[FillBackground] 
      then 
        paint.background = 
          this.asInstanceOf[FillBackground].fillBackgroundColor

      paint.write(0,0,renderText)

    /* #endregion */
          
    /* #region render border */

    paintStack.set(
      paintStack.get :+ { paint => 
      if focus.contains then renderBorder(paint)
    })

    def renderBorder(paint:PaintCtx):Unit = {
      val lt = childsLeftUpPos.get
      val rb = Position(lt.x+1+contentWidth.get, lt.y+1+contentHeight)
      val rt = Position(rb.x, lt.y)
      val lb = Position(lt.x, rb.y)
      val xCenter = ((lt.x - rt.x).abs / 2) + lt.x

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

      if childsInvisibleTail.get.nonEmpty 
      then paint.write(xCenter,lb.y, Symbols.Trinagles.down)

      if childsInvisibleHead.get.nonEmpty
      then paint.write(xCenter,lt.y, Symbols.Trinagles.up)
    }
    
    /* #endregion */

    /* #region render children */

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
        if widget.visible.value.get then paintChild()
      }

    /* #endregion */ 

    var keyMap:Map[KeyName,()=>Unit] = Map.empty

    focus.onAccept >> { 
      debug"focus.onAccept"
      showSubMenu 
    }
    focus.acceptChild.listen { _ =>
      debug"focus.onAccept"
      showSubMenu 
    }
    focus.onLost >> {
      debug"focus.onLost"
      checkHideSubMenu
    }

    def checkHideSubMenu:Unit =
      log"checkHideSubMenu"
      parent.get.foreach {
        case mc:MenuContainer => mc.checkHideSubMenu
        case _ =>
      }

      if !focus.contains 
      then 
        debug"checkHideSubMenu !focus.contains"
        hideSubMenu 

    def selectMenu: Unit = 
      log"selectMenu"
      children.headOption.foreach { _.focus.request }

    def hideSubMenu:Unit =
      log"hideSubMenu"
      children.foreach { mi => 
        debug"mi[${mi.renderText}].visible=false"
        mi.visible = false
      } 

    private lazy val menuLevel = Prop.eval( parent ) { _ => 
      this.toTreePath.listToLeaf.reverse.takeWhile( w => w.isInstanceOf[Menu] ).size
    }

    private lazy val parentMenu = Prop.eval(parent) { wOpt => wOpt.filter(_.isInstanceOf[MenuContainer]).map(_.asInstanceOf[MenuContainer]) }

    def showSubMenu:Unit =
      log"showSubMenu"
      menuLevel.get match
        case 0 => childsLeftUpPos.set(Position(0,1))
        case _ => 
          parentMenu.get.foreach { mc => 
            childsLeftUpPos.set(Position(mc.contentWidth.get,0))
          }
      
      upDownLayout
      bindUpDown

    private def childsMaxWidth = { 
      children.map(_.renderText.length()).maxOption.getOrElse(5) 
    }

    val contentWidthComputed = Prop.rw(false)
    val contentWidth = Prop.eval(contentWidthComputed,children){ case (_,_) => 
      //childsMaxWidth 
      children.map(_.renderText.length()).maxOption.getOrElse(5)
    }
    private def contentHeight = childsVisibleItems.get.size

    private lazy val childsVisibleOffset = Prop.rw(0)
    private lazy val childsVisibleCountMax = Prop.rw(10) 
    private lazy val childsCount = Prop.eval(children) { _.size }
    private lazy val childsVisibleOffsetMax = 
      Prop.eval(childsCount, childsVisibleCountMax) { 
        case (cnt,cmax) => 0 max (cnt - cmax)
    }
    private lazy val childsVisibleGroups = 
      Prop.eval( children, childsVisibleOffset, childsVisibleCountMax ) {
        case (childs, off, cmax) =>
          val off2 = off + cmax
          children.zipWithIndex.groupBy { case (mi,idx) => 
            if idx < off
            then 0
              else if idx >= off && idx < off2
                then 1
                else 2
          }
      }
      
    childsVisibleOffset.onChange( Session.addJob(upDownLayout) )
    childsVisibleCountMax.onChange( Session.addJob(upDownLayout) )

    private lazy val childsVisibleItems = 
      Prop.eval( childsVisibleGroups ) { case (grps) =>
        grps.get(1).map(_.map(_._1)).getOrElse(List())
      }

    private lazy val childsInvisibleHead = 
      Prop.eval( childsVisibleGroups ) { case (grps) => 
        grps.get(0).map(_.map(_._1)).getOrElse(List())
      }

    private lazy val childsInvisibleTail = 
      Prop.eval( childsVisibleGroups ) { case (grps) => 
        grps.get(2).map(_.map(_._1)).getOrElse(List())
      }

    private lazy val childsLeftUpPos = Prop.rw(Position(0,1))

    def upDownLayout: Unit =
      log"upDownLayout"
      
      val off = childsVisibleOffset.get
      val cmax = childsVisibleCountMax.get
      val off2 = off + cmax
      val lt = childsLeftUpPos.get

      debug"childsVisibleOffset off=$off off2=$off2"
      debug"childsVisibleCountMax $cmax"
      debug"childsLeftUpPos $lt"

      children.zipWithIndex.foreach { case (mi,idx) => 
        mi.location = Position( lt.x+1, lt.y+1+idx-childsVisibleOffset.get )
        mi.size = Size( contentWidth.get, 1 )
        mi.visible = idx>=off && idx<off2
        debug"mi[${mi.renderText}]: visible=${mi.visible.value.get} location=${mi.location.get} size=${mi.size.get}"
      }

    def bindUpDown:Unit =     
      log"bindUpDown" 
      children.zip(children.drop(1)).foreach { case (prevMi,nextMi) => 
        prevMi.keyMap = prevMi.keyMap + ( KeyName.Down -> ( ()=>{focusChildNext(nextMi)} ) )
        prevMi.keyMap = prevMi.keyMap + ( KeyName.Tab ->  ( ()=>{focusChildNext(nextMi)} ) )

        nextMi.keyMap = nextMi.keyMap + ( KeyName.Up         -> ( ()=>{focusChildPrev(prevMi)} ) )
        nextMi.keyMap = nextMi.keyMap + ( KeyName.ReverseTab -> ( ()=>{focusChildPrev(prevMi)} ) )
      }
      children.headOption.foreach { wid => wid.keyMap = wid.keyMap + ( KeyName.Up -> (()=>{MenuContainer.this.focus.request}) ) }

      children.foreach { mi =>
        mi.keyMap = mi.keyMap + ( KeyName.Enter -> (()=>{mi.selectMenu}) )

        if mi.isInstanceOf[MenuContainer]
        then
          mi.keyMap = mi.keyMap + ( KeyName.Right -> (()=>{mi.selectMenu}) )

        mi.keyMap = mi.keyMap + ( KeyName.Left -> (()=>{MenuContainer.this.focus.request}) )
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

    children.onInsert { mi =>
      mi.visible = false
    }

/* #endregion */

/* #region MenuAction */

class MenuAction(using config: MenuColorConfig)
  extends Menu:
    def this(text:String)(using config: MenuColorConfig) = {
      this()
      this.text = text
      this.size = Size(text.length(),1)
    }

    foregroundColor = config.foregroundColor
    backgroundColor = config.backgroundColor
    focusOwnerFgColor = config.focusOwnerFgColor
    focusOwnerBgColor = config.focusOwnerBgColor
    focusContainerFgColor = config.focusContainerFgColor
    focusContainerBgColor = config.focusContainerBgColor
    keyStrokeFgColor.set(config.keyStrokeFgColor)

    object keyStroke extends ReadWriteProp[Option[KeyStroke]](None):
      def apply(ks:KeyStroke):MenuAction =
        set(Some(ks))
        MenuAction.this

    /* #region paint text */

    def renderText: String = text.get + {
      keyStroke.get.map { ks => " " + ks.toString() }.getOrElse( "" )
    }

    paintStack.set(
      paintStack.get :+ { paint =>
        paintText(paint)
      }
    )

    def paintTextColor:Color =
      if this.isInstanceOf[WidgetInput]
      then
        val foc = this.asInstanceOf[WidgetInput].focus
        if this.isInstanceOf[FocusOwnerFgColor] && foc.isOwner 
        then this.asInstanceOf[FocusOwnerFgColor].focusOwnerFgColor.get 
        else 
          if this.isInstanceOf[FocusContainerFgColor] && foc.contains 
          then this.asInstanceOf[FocusContainerFgColor].focusContainerFgColor.get 
          else foregroundColor.get
      else
        foregroundColor.get

    lazy val keyStrokeFgColor = Prop.rw(Color.BlueBright)
    
    def paintText( paint:PaintCtx ):Unit =
      paint.foreground = paintTextColor

      if this.isInstanceOf[FillBackground] 
      then 
        paint.background = 
          this.asInstanceOf[FillBackground].fillBackgroundColor

      // this.parent.get
      //   .flatMap { w => if w.isInstanceOf[MenuContainer] then Some(w.asInstanceOf[MenuContainer]) else None }
      //   .map { mc => mc.contentWidth.get }.getOrElse( )

      val strName = (text.get.map { chr => ScreenChar(chr,paintTextColor,fillBackgroundColor) }).toList
      val strKs = keyStroke.get match
          case None => List()
          case Some(ks) => 
            List(ScreenChar(' ',paintTextColor,fillBackgroundColor)) ++
            ks.toString().map {
              chr => ScreenChar(chr,keyStrokeFgColor.get,fillBackgroundColor)
            }

      val remaind = (size.width() - strName.size - strKs.size) max 0
      val strRem = (" "*remaind).colors(paintTextColor,fillBackgroundColor)

      val str = strName ++ strRem ++ strKs
         
      paint.write(0,0,str)
    
    /* #endregion */

    focus.onLost >> checkHideSubMenu
    def checkHideSubMenu:Unit =
      parent.get.foreach {
        case mc:MenuContainer => mc.checkHideSubMenu
        case _ =>
      }

    var keyMap:Map[KeyName,()=>Unit] = Map.empty

    def selectMenu: Unit = 
      onAction.emit()

    var onAction = Listener.unit

    def action( ls: => Unit ):this.type =
      onAction.listen(ls)
      this

/* #endregion */

/* #region MenuBar */
class MenuBar(using config:MenuBarColorConfig) 
  extends Widget
  with WidgetChildren[Menu]
  with LocationRWProp
  with SizeRWProp
  with FillBackground
  with WidgetInput:

  private implicit val logger: Logger = LoggerFactory.getLogger("xyz.cofe.term.ui.MenuBar")

  backgroundColor = config.backgroundColor

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
      if widget.visible.value.get then paintChild()
    }

  protected var rootListeners : List[ReleaseListener] = List.empty
  protected var rootWidget : Option[RootWidget] = None

  def install( rootWidget:RootWidget ):Unit =
    log"install to root"

    if rootWidget.children.exists(_.isInstanceOf[MenuBar])
    then throw Error("MenuBar already installed")

    rootWidget.children.append(this)
    rootListeners = rootWidget.children.onInsert( reinstallOnInsert ) :: rootListeners
    rootListeners = rootWidget.children.onDelete( reinstallOnDelete ) :: rootListeners

    this.rootWidget = Some(rootWidget)

    location = Position(0,0)
    size = Size(rootWidget.size.get.width(),1)
    rootListeners = rootWidget.size.onChange { resizeDelayed() } :: rootListeners

    val inputSesListener: InputEvent => Boolean = this.processInput
    rootWidget.session.inputListeners = inputSesListener :: rootWidget.session.inputListeners

    val releaseInputSes = new ReleaseListener {
      def release(): Unit = {
        rootWidget.session.inputListeners = rootWidget.session.inputListeners.filterNot( ls => ls==inputSesListener )
      }
    }

    rootListeners = releaseInputSes :: rootListeners

  def uninstall():Unit =
    log"uninstall"
    rootListeners.foreach(_.release())
    rootListeners = List()
    rootWidget = None

  private var movingToFront : Boolean = false

  protected def reinstallOnInsert( wid:Widget ):Unit =
    if movingToFront 
    then
      debug"skip reinstallOnInsert $wid"
    else
      log"reinstallOnInsert ${wid}"
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
    if movingToFront
    then
      debug"skip reinstallOnDelete $wid"
    else
      log"reinstallOnDelete ${wid}"
      if wid==this 
      then uninstall()
      else moveToFrontDelayed()

  protected def moveToFrontDelayed():Unit =
    log"moveToFrontDelayed" 
    rootWidget.map(_.session).foreach { ses => ses.addJob( moveToFront ) }

  protected def moveToFront():Unit =
    log"moveToFront" 
    try 
      movingToFront = true
      rootWidget.foreach { rootWid => 
        rootWid.children.delete( MenuBar.this )
        rootWid.children.append( MenuBar.this )
      }
    finally
      movingToFront = false

  protected def resizeDelayed():Unit =
    log"resizeDelayed" 
    rootWidget.map(_.session).foreach(_.addJob(resize))

  protected def resize():Unit =
    log"resize" 
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

  private var keyStrokeMapOpt : Option[KeyStrokeMap[MenuAction]] = None
  private def buildKeyStrokeMap: KeyStrokeMap[MenuAction] =
    log"buildKeyStrokeMap"
    KeyStrokeMap( this.walk.path
      .filter(_.node.isInstanceOf[MenuAction])
      .map(_.node.asInstanceOf[MenuAction])
      .toList
      .filter(_.keyStroke.get.isDefined)
      .map(ma => (ma.keyStroke.get.get, ma))
      .toMap)

  private def keyStrokeMap: KeyStrokeMap[MenuAction] =
    keyStrokeMapOpt.getOrElse {
      keyStrokeMapOpt = Some(buildKeyStrokeMap)
      keyStrokeMapOpt.get
    }

  private var keyStrokeParserOpt: Option[KeyStrokeMap.InputParser[MenuAction]] = None
  private def keyStrokeParser: KeyStrokeMap.InputParser[MenuAction] =
    keyStrokeParserOpt.getOrElse {
      log"build KeyStrokeInputParser"
      keyStrokeParserOpt = Some( KeyStrokeMap.InputParser(keyStrokeMap) )
      keyStrokeParserOpt.get
    }

  def processInput(inputEvent:InputEvent):Boolean = 
    log"processInput $inputEvent"
    var processed = false
    keyStrokeParser.input(inputEvent) { ma => 
      log"matched $ma"
      ma.selectMenu
      processed = true
    }
    debug"processed $processed"
    processed

/* #endregion */
