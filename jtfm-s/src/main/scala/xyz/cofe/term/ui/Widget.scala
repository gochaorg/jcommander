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
import xyz.cofe.term.geom._
import xyz.cofe.term.ui.prop._

trait Widget:
  val parent:ReadWriteProp[Option[WidgetChildren[?]]] = ReadWriteProp(None)
  def location:Prop[Position]
  def size:Prop[Size]  
  lazy val locationRect = Prop.eval( location, size ){ case (loc,size) => size.leftUpRect(loc) }

  val visible = VisibleClient(this)
  visible.value.onChange { repaint }
  def visible_=( value:Boolean ):Unit = visible.value.set(value)

  def paint(paintCtx:PaintCtx):Unit = {}
  def repaint:Unit = Session.currentSession.foreach( ses => ses.repaint(this) )
  def toTreePath:TreePath[Widget] = 
    var path = List(this)
    var n:Widget = this
    while n.parent.get.isDefined do
      path = n.parent.get.get :: path
      n = n.parent.get.get
    new RTreePath[Widget](path)
  def walk = LikeTree.Walk(this)

implicit def sizeProp2Size( prop:Prop[Size] ):Size = prop.get
implicit def locationProp2Position( prop:Prop[Position] ):Position = prop.get

given LikeTree[Widget] with
  def nodes(w:Widget):List[Widget] = 
    w match
      case cw: WidgetChildren[?] => cw.children.toList
      case _ => List()
trait RootWidget extends Widget with WidgetChildren[Widget] with SizeRWProp with LocationRWProp:
  def session: Session

