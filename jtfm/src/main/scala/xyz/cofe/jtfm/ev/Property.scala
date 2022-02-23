package xyz.cofe.jtfm.ev

/**
 * Свойство
 *
 * @tparam VALUE тип свойства
 */
trait Property[SELF <: Property[SELF, VALUE], VALUE] {
  /**
   * Чтение значения свойства
   *
   * @return значение
   */
  def value: VALUE
  
  /**
   * Обновление значение свойства
   *
   * @param v новое значение
   */
  def value(v: VALUE): Unit
  
  /**
   * Подписка на уведомления о изменении значения
   *
   * @param l подписчик
   * @return отписка от уведомлений
   */
  def listen(l: (SELF, VALUE, VALUE) => Unit): () => Unit
}
