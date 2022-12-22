package xyz.cofe.term.ui

import xyz.cofe.term.common.InputResizeEvent
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.KeyName

import SesInput._
import scala.reflect.ClassTag

trait SesInput extends SesPaint:
  private var focusOwnerValue : Option[WidgetInput] = None
  def focusOwner:Option[WidgetInput] = focusOwnerValue

  protected  def processInput():Unit =
    val inputEvOpt = console.read()
    if( inputEvOpt.isPresent() ){
      val inputEv = inputEvOpt.get()
      inputEv match
        case resizeEv:InputResizeEvent =>
          val size = resizeEv.size()
          screenBuffer.resize(size)
          rootWidget.size.set(size)
        case ke:InputKeyEvent =>
          ke.getKey() match
            case KeyName.Tab => focusNext()
            case KeyName.ReverseTab => focusPrev()
            case _ =>
          
        case _ => 
          rootWidget.children.nested.foreach { path => 
            path.last match
              case wInput:WidgetInput =>
                wInput.input(inputEv)
              case _ =>
          }
    }

  private def focusNext():Unit = 
    NavigateFrom(focusOwnerValue.getOrElse(rootWidget))
      .forward.typed[WidgetInput].visibleOnly
      .nextOption().foreach(switchFocusTo)

  private def focusPrev():Unit = 
    NavigateFrom(focusOwnerValue.getOrElse(rootWidget))
      .backward.typed[WidgetInput].visibleOnly
      .nextOption().foreach(switchFocusTo)

  private def switchFocusTo(widInput:WidgetInput) =
    focusOwnerValue = Some(widInput)
    widInput.repaint

object SesInput:
  opaque type NavigateFrom = Widget
  object NavigateFrom:
    def apply(wid:Widget):NavigateFrom = wid
  extension (navFrom:NavigateFrom)
    def forward:Navigator[Widget] = Navigator(navFrom, w => w.toTreePath.nextByDeep.map(_.node) )
    def backward:Navigator[Widget] = Navigator(navFrom, w => w.toTreePath.prevByDeep.map(_.node) )

  case class Navigator[W <: Widget : ClassTag]( from:Widget, move:Widget=>Option[Widget], skipFirst:Boolean=true, filter:W=>Boolean=(w:W)=>true ) extends Iterator[W]:
    def fetch( from:Widget ):Option[W] =
      var cur = from
      var stop = false
      val trgtCls = summon[ClassTag[W]].runtimeClass
      var res : Option[W] = None
      var cycle = 0
      def fetchNext = {
        move(cur) match
          case None => 
            stop = true
            res = None
          case Some(next) =>
            cur = next
      }
      while !stop do
        cycle += 1        
        if trgtCls.isAssignableFrom( cur.getClass() ) && filter(cur.asInstanceOf[W])
        then 
          res = Some(cur.asInstanceOf[W])
          if cycle==1 
          then 
            if skipFirst 
            then 
              res = None
              fetchNext 
            else 
              stop = true
          else stop = true
        else
          fetchNext
      res

    var current = fetch(from)
    
    def hasNext: Boolean = current.isDefined
    def next(): W = 
      val res = current.get
      current = fetch(res)
      res

    def visibleOnly:Navigator[W] = copy(
      filter = wid => wid match
        case vp:VisibleProp => vp.visible.inTree
        case _ => false      
    )

    def typed[W <: Widget:ClassTag]:Navigator[W] = Navigator[W](from, move, skipFirst, (w:W)=>true )
