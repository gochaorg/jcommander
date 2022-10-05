package xyz.cofe.jtfm

import scala.collection.Iterable

class ObserverMap[K,V] extends Iterable[(K,V)]:
  private val data = scala.collection.mutable.Map[K,V]()

  private var listeners = List[ObserverCollEvent[K,V]=>Unit]()
  def listen(ls:ObserverCollEvent[K,V]=>Unit):ObserverCollListener[K,V] =
    ObserverCollListener(ls, l => {
      listeners = l :: listeners
    })

  private def emit(ev:ObserverCollEvent[K,V]):Unit =
    listeners.foreach( ls => ls(ev) )

  def keys = data.keySet
  def values = data.values
  def iterator = data.iterator
  def apply(key:K) = data.apply(key)
  def get(key:K) = data.get(key)
  override def size:Int = data.size

  def put(key:K, value:V) = 
    val prev = data.put(key,value)
    prev match
      case Some(old) =>
        emit(ObserverCollEvent.Update(key,old,value))
      case None =>
        emit(ObserverCollEvent.Insert(key,value))
    prev

  def putAll(map:Map[K,V]) =
    map.foreach { case(k,v) => put(k,v) }

  def putAll(map:Iterable[(K,V)]) =
    map.foreach { case(k,v) => put(k,v) }

  def remove(key:K) = 
    val prev = data.remove(key)
    prev match
      case Some(old) => emit(ObserverCollEvent.Delete(key,old))
      case _ => ()
    prev

  def clear() = 
    data.foreach { case(k,v) => emit(ObserverCollEvent.Delete(k,v)) }
    data.clear()