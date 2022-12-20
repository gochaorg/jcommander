package xyz.cofe.term.ui

import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.lazyp.Prop
import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.InputEvent

trait Widget:
  val parent:ReadWriteProp[Option[Widget]] = ReadWriteProp(None)
  def location:Prop[Position]
  def size:Prop[Size]
  def paint(paintCtx:PaintCtx):Unit = {}
  def repaint:Unit = Session.currentSession.foreach( ses => ses.repaint(this) )

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

trait VisibleRWProp extends Widget:
  val visible:ReadWriteProp[Boolean] = ReadWriteProp(true)
  visible.onChange { repaint }

trait WidgetInput extends Widget:
  def input(inputEvent:InputEvent):Unit

trait RootWidget extends Widget with WidgetChildren[Widget] with SizeRWProp with LocationRWProp
