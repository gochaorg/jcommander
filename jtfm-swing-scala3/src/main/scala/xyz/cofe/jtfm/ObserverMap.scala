package xyz.cofe.jtfm

import scala.collection.Iterable

class ObserverMap[K,V] extends Iterable[(K,V)]:
  private val data = scala.collection.mutable.Map[K,V]()

  def keys = data.keySet
  def values = data.values
  def iterator = data.iterator
  def apply(key:K) = data.apply(key)
  def get(key:K) = data.get(key)
  override def size:Int = data.size

  def put(key:K, value:V) = data.put(key,value)
  def remove(key:K) = data.remove(key)

  def clear() = data.clear()