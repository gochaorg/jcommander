package xyz.cofe.jtfm.wid

/**
 * Событие ввода
 * @tparam EVENT
 */
trait UserInput[EVENT] {
  /**
   * Событие ввода
   * @param ev Событие ввода
   * @return true - событие обработа, false - не обработано
   */
  def input(ev:EVENT):Boolean = false
}
