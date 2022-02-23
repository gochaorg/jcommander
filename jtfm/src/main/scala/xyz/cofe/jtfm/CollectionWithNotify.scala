package xyz.cofe.jtfm

/**
 * Коллекция с уведомлениями
 *
 * @tparam N    тип элемента
 * @tparam SELF собственный тип
 */
trait CollectionWithNotify[SELF, N] extends Collection[N] {
  /**
   * Тип подписчика
   */
  type LISTENER=(SELF, Int, Option[N], Option[N]) => Unit
  
  /**
   * Подписаться на события
   * @param l подписчик
   * @return отписка
   */
  def listen(l: LISTENER): () => Unit
}
