package xyz.cofe.jtfm.ev

import xyz.cofe.jtfm.ev._

/**
 * Мутабельная коллекция
 *
 * @tparam N тип элемента
 */
trait MutCollection[N] extends Collection[N] {
  def append(n: N): Unit
  
  def append(coll: Iterable[N]): Unit = {
    coll.foreach(append)
  }
  
  def prepend(n: N): Unit
  
  def removeAt(idx: Int): Unit
  
  def set(idx: Int, n: N): Unit
}
