package xyz.cofe.term.ui

import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.lazyp.Prop
import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.InputEvent

trait Widget:
  //val parent:ReadWriteProp[Option[Widget]] = ReadWriteProp(None)
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
  val children:ReadWriteProp[List[C]] = ReadWriteProp(List())
  children.onChange { repaint }

trait VisibleRWProp extends Widget:
  val visible:ReadWriteProp[Boolean] = ReadWriteProp(true)
  visible.onChange { repaint }

trait WidgetInput extends Widget:
  def input(inputEvent:InputEvent):Unit

trait RootWidget extends Widget with WidgetChildren[Widget] with SizeRWProp with LocationRWProp
extension [C <: Widget]( children:ReadWriteProp[List[C]] )
  def append( child:C ):Unit =
    val childrenList = children.get
    children.set( childrenList.appended(child) )      

  def remove( child:C ):Unit =
    children.set( children.get.filterNot( c => c==child ) )

  def delete( index:Int ):Unit =
    val (left,right) = children.get.splitAt(index)
    children.set( 
      left  ++
      ( if right.nonEmpty then right.tail else right )
    )

  def clear():Unit =
    children.set(List())

  def insert( index:Int, child:C ):Unit =
    if index<=0 then
      children.set( child :: children.get )
    else
      if index>=children.get.size
        then children.set( children.get.appended(child) )
        else 
          val (left,right) = children.get.splitAt(index)
          children.set( left ++ List(child) ++ right )

  def set( index:Int, child:C ):Unit =
    if index<0
    then children.set( child :: children.get )
    else
      if index>=children.get.size
      then children.set( children.get.appended(child) )
      else 
        val (left,right) = children.get.splitAt(index)
        children.set( 
          left  ++
          List(child) ++
          ( if right.nonEmpty then right.tail else right )
        )

  def nested: NestedWidgetIterator = NestedWidgetIterator( children.get )