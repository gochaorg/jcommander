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
   * Подписка на уведомления о изменении значения
   *
   * @param l подписчик
   * @return отписка от уведомлений
   */
  def listen(l: (SELF, VALUE, VALUE) => Unit): () => Unit
  
  /**
   * Подписка на уведомления о изменении значения
   *
   * @param l подписчик
   * @return (SELF, отписка от уведомлений)
   */
  def observe( l: (SELF,VALUE,VALUE)=>Unit ): (SELF, ()=>Unit) = {
    val ls = listen(l)
    (this.asInstanceOf[SELF], ls)
  }
}
