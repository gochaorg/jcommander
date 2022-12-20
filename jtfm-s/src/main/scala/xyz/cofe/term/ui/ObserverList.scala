package xyz.cofe.term.ui

import xyz.cofe.lazyp.Prop
import xyz.cofe.lazyp.ReleaseListener

trait ObserverList[A] extends Iterable[A] with Prop[ObserverList[A]]:
  def insert(index:Int,item:A):Unit
  def insert(index:Int,items:Iterable[A]):Unit
  def delete(item:A):Unit
  def delete(items:Iterable[A]):Unit
  def deleteAt(index:Int):Unit
  def update(index:Int, item:A):Unit
  def onInsert(ls: A=>Unit):ReleaseListener
  def onDelete(ls: A=>Unit):ReleaseListener

class ObserverListImpl[A] extends ObserverList[A]:
  override def get: ObserverList[A] = this

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

  def delete(item:A):Unit = 
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

  def delete(items:Iterable[A]):Unit = 
    items.foreach(delete)

  def deleteAt(index:Int):Unit = 
    val (left,right) = items.splitAt(index)
    items = left ++ (if right.nonEmpty then right.tail else right)
    if right.nonEmpty then
      fireDeleted(right.head)
      fireChanged()

  def update(index:Int, item:A):Unit = 
    val (left,right) = items.splitAt(index)
    items = left ++ (if right.nonEmpty then item :: right.tail else right)
    if right.nonEmpty then
      fireDeleted(right.head)
      fireInserted(item)
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
