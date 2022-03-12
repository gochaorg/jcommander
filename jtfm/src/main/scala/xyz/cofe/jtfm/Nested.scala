package xyz.cofe.jtfm

import xyz.cofe.jtfm.ev.CollectionWithNotify
import xyz.cofe.jtfm.ev.BasicCollection

/**
 * Список дочерних узлов дерева
 * @tparam SELF Собственный тип
 * @tparam N Тип дочеерних узлов
 */
trait Nested[SELF,N] {
  lazy val nested = BasicCollection[N]()
}
