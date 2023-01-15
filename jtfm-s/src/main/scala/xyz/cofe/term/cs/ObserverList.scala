package xyz.cofe.term.cs

import xyz.cofe.lazyp.Prop
import xyz.cofe.lazyp.ReleaseListener
import scala.collection.immutable.SortedSet

trait ObserverList[A] extends Iterable[A] with Prop[ObserverList[A]]:
  def getAt(index:Int):Option[A]
  def insert(index:Int,item:A):Unit
  def insert(index:Int,items:Iterable[A]):Unit
  def delete[A1 >: A](item:A1):Unit
  def delete[A1 >: A](items:Iterable[A1]):Unit
  def deleteAt(index:Int):Unit
  def deleteAt(indexes:Iterable[Int]):Unit
  def update(index:Int, item:A):Unit
  def clear():Unit
  def append(item:A):Unit = insert(Int.MaxValue,item)
  def append(items:Iterable[A]):Unit = insert(Int.MaxValue,items)
  def contains[A1 >: A](item:A1):Boolean
  def onInsert(ls: A=>Unit):ReleaseListener
  def onDelete(ls: A=>Unit):ReleaseListener

class ObserverListImpl[A] extends ObserverList[A]:
  override def get: ObserverList[A] = this

  def getAt(index:Int):Option[A] =
    if index<0 || index>=items.size
    then None
    else Some(items(index))

  var onChangeListeners = List[()=>Unit]()
  override def onChange(listener: => Unit): ReleaseListener = 
    val ls : ()=>Unit = ()=>listener

    onChangeListeners = ls :: onChangeListeners
    new ReleaseListener {
      def release(): Unit = {
        onChangeListeners = onChangeListeners.filterNot( l => l==ls )
      }
    }

  var items = List[A]()
  override def iterator: Iterator[A] = items.iterator

  def contains[A1 >: A](item:A1):Boolean = items.contains(item)

  def insert(index:Int,item:A):Unit = 
    val (left,right) = items.splitAt(index)
    items = left ++ List(item) ++ right
    fireInserted(item)
    fireChanged()

  def insert(index:Int,insItems:Iterable[A]):Unit = 
    val (left,right) = items.splitAt(index)
    items = left ++ insItems ++ right
    insItems.foreach(fireInserted)
    if insItems.nonEmpty then fireChanged()

  def delete[A1 >: A](item:A1):Unit = 
    var deleted = List[A]()
    items = items.filter { i => 
      if( i==item ){
        deleted = i :: deleted
        false
      }else{
        true
      }
    }
    deleted.foreach(fireDeleted)
    if deleted.nonEmpty then fireChanged()

  def delete[A1 >: A](items:Iterable[A1]):Unit = 
    items.foreach(delete)

  def deleteAt(index:Int):Unit = 
    val (left,right) = items.splitAt(index)
    items = left ++ (if right.nonEmpty then right.tail else right)
    if right.nonEmpty then
      fireDeleted(right.head)
      fireChanged()

  def deleteAt(indexes:Iterable[Int]):Unit =
    SortedSet(indexes).toList.reverse.foreach(deleteAt)
    
  def update(index:Int, item:A):Unit = 
    val (left,right) = items.splitAt(index)
    items = left ++ (if right.nonEmpty then item :: right.tail else right)
    if right.nonEmpty then
      fireDeleted(right.head)
      fireInserted(item)
      fireChanged()

  def update(index:Int, newItems:Iterable[A]):Unit =
    val (left,right) = items.splitAt(index)
    items = left ++ (if right.nonEmpty then newItems.toList ++ right.tail else right)
    if right.nonEmpty then
      fireDeleted(right.head)
      newItems.foreach(fireInserted)
      fireChanged()

  def clear():Unit =
    val oldItems = items
    items = List()
    oldItems.foreach(fireDeleted)
    fireChanged()

  def fireInserted(item:A) =
    onInsertListeners.foreach(_(item))  

  def fireDeleted(item:A) =
    onDeleteListeners.foreach(_(item))

  def fireChanged()=
    onChangeListeners.foreach(l => l())
  
  var onInsertListeners = List[A=>Unit]()
  def onInsert(ls: A=>Unit):ReleaseListener = 
    onInsertListeners = ls :: onInsertListeners
    new ReleaseListener {
      def release(): Unit = {
        onInsertListeners = onInsertListeners.filterNot( l => l==ls )
      }
    }

  var onDeleteListeners = List[A=>Unit]()
  def onDelete(ls: A=>Unit):ReleaseListener = 
    onDeleteListeners = ls :: onDeleteListeners
    new ReleaseListener {
      def release(): Unit = {
        onDeleteListeners = onDeleteListeners.filterNot( l => l==ls )
      }
    }

object ObserverList:
  def empty[A]:ObserverList[A] = new ObserverListImpl()