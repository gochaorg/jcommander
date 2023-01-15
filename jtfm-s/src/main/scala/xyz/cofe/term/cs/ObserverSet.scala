package xyz.cofe.term.cs

import xyz.cofe.lazyp.Prop
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.ui.Listener
import scala.collection.immutable.SortedSet
import scala.collection.immutable.TreeSet

object ObserverSet:
  def unsorted[A]:ObserverSet[A]     = new ObserverSetBase[A,Set]( Set.empty[A] )
  def sorted[A:Ordering]:ObserverSortedSet[A] = new ObserverSetImplSortted[A,TreeSet]( TreeSet.empty[A] )

trait ObserverSet[A] extends Iterable[A] with Prop[ObserverSet[A]]:
  def contains(a:A):Boolean
  def include(a:A):Boolean
  def include(items:Iterable[A]):Int
  def exclude(a:A):Boolean
  def exclude(items:Iterable[A]):Int

  def onInsert(ls: A=>Unit):ReleaseListener
  def onDelete(ls: A=>Unit):ReleaseListener

trait ObserverSortedSet[A:Ordering] extends ObserverSet[A]

trait SetOp[A,S[_]]:
  def filter(s:S[A], test:A=>Boolean):S[A]
  def join(s:S[A], a:A):S[A]

object SetOp:
  given simpleSet[A]:SetOp[A,Set] with
    override def filter(s: Set[A], test: A => Boolean): Set[A] = 
      s.filter(test)

    override def join(s: Set[A], a: A): Set[A] = 
      s ++ Set(a)

  given sortedSet[A]:SetOp[A,SortedSet] with
    override def filter(s: SortedSet[A], test: A => Boolean): SortedSet[A] = 
      s.filter(test)
    override def join(s: SortedSet[A], a: A): SortedSet[A] = 
      s ++ Set(a)

  given treeSet[A]:SetOp[A,TreeSet] with
    override def filter(s: TreeSet[A], test: A => Boolean): TreeSet[A] = 
      s.filter(test)
    override def join(s: TreeSet[A], a: A): TreeSet[A] = 
      s ++ Set(a)

class ObserverSetBase[A,S[A] <: Set[A]]( initial:S[A] )(using setOp:SetOp[A,S]) extends ObserverSet[A]:
  var values:S[A] = initial

  val deleteListeners = Listener.paramter[A]
  override def onDelete(ls: A => Unit): ReleaseListener = 
    deleteListeners(ls)

  val insertListeners = Listener.paramter[A]
  override def onInsert(ls: A => Unit): ReleaseListener = 
    insertListeners(ls)

  val changeListeners = Listener()
  override def onChange(listener: => Unit): ReleaseListener = 
    changeListeners(listener)


  override def contains(a: A): Boolean = 
    values.contains(a)

  override def exclude(a: A): Boolean = 
    if ! contains(a) then
      false
    else
      val delItems = values.filter(_ == a)
      values = setOp.filter(values, e => e!=a )
      delItems.foreach(deleteListeners.emit)
      if delItems.nonEmpty then changeListeners.emit()
      true

  override def exclude(items: Iterable[A]): Int = 
    items.map(x => exclude(x)).map {
      case false => 0
      case true => 1
    }.sum

  override def include(a: A): Boolean = 
    if contains(a) 
    then false
    else 
      values = setOp.join(values, a)
      insertListeners.emit(a)
      changeListeners.emit()
      true

  override def include(items: Iterable[A]): Int = 
    items.map(x => include(x)).map {
      case false => 0
      case true => 1
    }.sum

  override def get: ObserverSet[A] = this
  override def iterator: Iterator[A] = values.iterator

class ObserverSetImplSortted[A:Ordering,S[A] <: Set[A]]( initial:S[A] )(using setOp:SetOp[A,S]) extends ObserverSetBase(initial) with ObserverSortedSet[A]