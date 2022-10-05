package xyz.cofe.jtfm

case class ObserverCollListener[K,V](ls:ObserverCollEvent[K,V]=>Unit, private val remove:(ObserverCollEvent[K,V]=>Unit)=>Unit ):
  def close:Unit =
    remove(ls)

