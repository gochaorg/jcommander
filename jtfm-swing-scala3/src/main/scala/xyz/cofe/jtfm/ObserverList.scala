package xyz.cofe.jtfm.ui

import scala.collection.mutable.ArrayBuffer

class ObserverList[A]() extends Iterable[A]:
  private val data = ArrayBuffer[A]()
  enum Event:
    case Insert(idx:Int, a:A) extends Event
    case Delete(idx:Int, a:A) extends Event
    case Update(idx:Int, a:A, b:A) extends Event

  case class Listener(ls:Event=>Unit):
    def close:Unit =
      listeners = listeners.filterNot(l => l==ls)

  private var listeners = List[Event=>Unit]()
  def listen(ls:Event=>Unit):Listener =
    listeners = ls :: listeners
    Listener(ls)

  private def emit(ev:Event):Unit =
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
    emit(Event.Insert(data.size-1, a))
    Right(this)

  def insert( index:Int, a:A ):Either[String,ObserverList[A]] =
    if index<0 then 
      Left( s"index<0" )
    else if index>=data.size then
      insert(a)
    else
      data.insert( index,a )
      emit(Event.Insert(index, a))
      Right(this)

  def delete( index:Int ):Either[String,A] =
    if index<0
      then Left(s"index($index) < 0")
      else if index>=data.size
        then Left(s"index($index) >= data.size(${data.size})")
        else 
          val old = data(index)
          data.remove(index)
          emit(Event.Delete(index,old))
          Right(old)

  def update( index:Int, a:A ):Either[String,A] =
    if index<0
      then Left(s"index($index) < 0")
      else if index>=data.size
        then Left(s"index($index) >= data.size(${data.size})")
        else 
          val old = data(index)
          data.update(index, a)
          emit(Event.Update(index,old,a))
          Right(old)

  def clear():Either[String,List[A]] =
    val removed = data.toList
    removed.zip(0 until removed.size).reverse.foreach { case (el,idx) => 
      emit(Event.Delete(idx, el))
    }
    data.clear()
    Right(removed)
    