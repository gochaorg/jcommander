package xyz.cofe.jtfm.ev

trait MutProperty[SELF <: MutProperty[SELF, VALUE], VALUE] extends Property[SELF,VALUE] {
  /**
   * Обновление значение свойства
   *
   * @param v новое значение
   */
  def value(v: VALUE): Unit
}
