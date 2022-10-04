package xyz.cofe.jtfm.ui

import scala.collection.mutable.ArrayBuffer

enum ObserverListEvent[A]:
  case Insert(idx:Int, a:A) extends ObserverListEvent[A]
  case Delete(idx:Int, a:A) extends ObserverListEvent[A]
  case Update(idx:Int, a:A, b:A) extends ObserverListEvent[A]

class ObserverList[A]() extends Iterable[A]:
  private val data = ArrayBuffer[A]()

  case class Listener(ls:ObserverListEvent[A]=>Unit):
    def close:Unit =
      listeners = listeners.filterNot(l => l==ls)

  private var listeners = List[ObserverListEvent[A]=>Unit]()
  def listen(ls:ObserverListEvent[A]=>Unit):Listener =
    listeners = ls :: listeners
    Listener(ls)

  private def emit(ev:ObserverListEvent[A]):Unit =
    listeners.foreach( ls => ls(ev) )

  override def iterator: Iterator[A] = data.iterator

  override def size:Int = data.size

  def get(index:Int):Option[A] =
    if index<0
      then None
      else if index>=data.size
        then None
        else Some(data(index))

  def apply(index:Int) = data(index)

  def insert( a:A ):Either[String,ObserverList[A]] =
    data.addOne(a)
    emit(ObserverListEvent.Insert(data.size-1, a))
    Right(this)

  def insert( index:Int, a:A ):Either[String,ObserverList[A]] =
    if index<0 then 
      Left( s"index<0" )
    else if index>=data.size then
      insert(a)
    else
      data.insert( index,a )
      emit(ObserverListEvent.Insert(index, a))
      Right(this)

  def delete( index:Int ):Either[String,A] =
    if index<0
      then Left(s"index($index) < 0")
      else if index>=data.size
        then Left(s"index($index) >= data.size(${data.size})")
        else 
          val old = data(index)
          data.remove(index)
          emit(ObserverListEvent.Delete(index,old))
          Right(old)

  def delete( a:A ):Either[String,List[A]] =
    val removed = (0 until data.size).reverse.map { idx => 
      (idx,data(idx))
    }.filter { case(idx,item) => 
      item == a
    }.map { case(idx,item) => {      
      data.remove(idx)
      emit(ObserverListEvent.Delete(idx,item))
      item
    }}.toList
    Right(removed)

  def update( index:Int, a:A ):Either[String,A] =
    if index<0
      then Left(s"index($index) < 0")
      else if index>=data.size
        then Left(s"index($index) >= data.size(${data.size})")
        else 
          val old = data(index)
          data.update(index, a)
          emit(ObserverListEvent.Update(index,old,a))
          Right(old)

  def clear():Either[String,List[A]] =
    val removed = data.toList
    removed.zip(0 until removed.size).reverse.foreach { case (el,idx) => 
      emit(ObserverListEvent.Delete[A](idx, el))
    }
    data.clear()
    Right(removed)

  def swap(first:Int, second:Int):Either[String,Unit] =
    if first<0 then Left(s"first < 0")
    else if first>=data.size then Left(s"first >= data.size")
      else if second<0 then Left(s"second < 0")
      else if second>=data.size then Left(s"second >= data.size")
        else
          val a = data(first)
          val b = data(second)
          data.update(first,b)
          data.update(second,a)
          emit(ObserverListEvent.Update(first,a,b))
          emit(ObserverListEvent.Update(second,b,a))
          Right(())

  def indexOf(a:A, from:Int=0):Option[Int] =
    if from<0 then None
    else
      var idx = from
      var found = None:Option[Int]
      while idx<size && found.isEmpty do
        val itm = data(idx)
        idx += 1
        if itm==a then found = Some(idx)
      found
    
object ObserverList:
  def apply[A]():ObserverList[A] =
    new ObserverList[A]()
    
  def apply[A]( iter:Iterable[A] ):ObserverList[A] =
    val list = new ObserverList[A]()
    iter.foreach(list.insert)
    list