package xyz.cofe.jtfm.ev

/**
 * Управление подписчиком
 */
trait Listener {
  /**
   * Добавление подписчика
   */
  def add():Unit
  
  /**
   * Удаление подписчика
   */
  def remove():Unit
}

object Listener {
  def apply[L]( l:L, get:()=>List[L], set:(List[L])=>Unit ):Listener = new Listener {
    /**
     * Добавление подписчика
     */
    override def add(): Unit = set( l :: get() )
  
    /**
     * Удаление подписчика
     */
    override def remove(): Unit = set( get().filter( _ != l ) )
  }
}
