package xyz.cofe.term.ui

import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.lazyp.Prop
import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.cs.ObserverList
import xyz.cofe.term.cs.LikeTree
import xyz.cofe.term.cs.TreePath
import xyz.cofe.term.cs.RTreePath

trait Widget:
  val parent:ReadWriteProp[Option[Widget]] = ReadWriteProp(None)
  def location:Prop[Position]
  def size:Prop[Size]
  def paint(paintCtx:PaintCtx):Unit = {}
  def repaint:Unit = Session.currentSession.foreach( ses => ses.repaint(this) )
  def toTreePath:TreePath[Widget] = 
    var path = List(this)
    var n:Widget = this
    while n.parent.get.isDefined do
      path = n.parent.get.get :: path
      n = n.parent.get.get
    new RTreePath[Widget](path)

given LikeTree[Widget] with
  def nodes(w:Widget):List[Widget] = 
    w match
      case cw: WidgetChildren[?] => cw.children.toList
      case _ => List()

trait LocationRWProp extends Widget:
  val location:ReadWriteProp[Position] = ReadWriteProp(Position(0,0))
  location.onChange { repaint }

trait SizeRWProp extends Widget:
  val size:ReadWriteProp[Size] = ReadWriteProp(Size(1,1))
  size.onChange { repaint }

trait WidgetChildren[C <: Widget] extends Widget:
  val children:ObserverList[C] = ObserverList.empty
  children.onChange { repaint }
  children.onInsert { ch => ch.parent.set(Some(this)) }
  children.onDelete { ch => ch.parent.compareAndSet(Some(this),None) }

trait VisibleProp extends Widget:
  val visible = VisibleClient(this)
  visible.value.onChange { repaint }

  def visible_=( value:Boolean ):Unit = visible.value.set(value)

class VisibleClient( widget:Widget ):
  val value:ReadWriteProp[Boolean] = ReadWriteProp(true)
  def inTree:Boolean = 
    widget.toTreePath.listToLeaf.forall {
      case wv:VisibleProp => wv.visible.value.get
      case _ => true
    }

implicit def visibleClient2Bool( vc:VisibleClient ):Boolean = vc.value.get

trait RootWidget extends Widget with WidgetChildren[Widget] with SizeRWProp with LocationRWProp:
  def session: Session

