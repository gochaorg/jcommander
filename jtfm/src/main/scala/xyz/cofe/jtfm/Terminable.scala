package xyz.cofe.jtfm

/**
 * Завершение сессии
 * @tparam S сессиия
 */
trait Terminable[S] {
  /**
   * Завершения сессии
   * @param s сессия
   */
  def terminate(s:S):Unit
}
