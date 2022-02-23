package xyz.cofe.jtfm

import scala.collection.immutable.Iterable

/**
 * Коллекция
 * @tparam N тип элемента коллекция
 */
trait Collection[N] extends Iterable[N] {
  /**
   * Возвращает кол-во элементов в коллекции
   * @return кол-во элементов
   */
  def size:Int
  
  /**
   * Получение элемента коллекции
   * @param idx индекс элемента
   * @return элемент
   */
  def apply(idx:Int):N
}
