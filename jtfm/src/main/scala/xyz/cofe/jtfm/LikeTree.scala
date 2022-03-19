package xyz.cofe.jtfm

/**
 * Адаптер для навигации по дереву
 */
trait LikeTree[N] {
  /**
   * Возвращает родительский узел
   * @param n узел
   * @return родительский узел
   */
  def parent(n:N):Option[N]

  /**
   * @param n узел
   * @return кол-во дочерних узлов
   */
  def childrenCount(n:N):Int

  /**
   * @param param родительский узел
   * @param child дочерний узел
   * @return индекс дочернего узла
   */
  def indexOf(parent:N, child:N):Option[Int]

  /**
   * @param param родительский узел
   * @param idx индекс дочернего узла
   * @return дочерний узел
   */
  def child(parent:N, idx:Int):Option[N]

  /**
   * @param n узел
   * @param idx смещение относительно узла
   * <ul>
   *   <li>0 - сам узел</li>
   *   <li>-1 - соседний узел слева</li>
   *   <li> 1 - соседний узел справа</li>
   * </ul>
   * @return сосдний узел
   */
  def sib(node:N, idx:Int):Option[N] = parent(node) match {
    case Some(prnt) => indexOf(prnt,node) match {
      case Some(node_idx) =>
        val t_idx = node_idx + idx
        val c_cnt = childrenCount(prnt)
        if( t_idx<0 || t_idx>=c_cnt ){
          None
        }else{
          child(prnt,t_idx)
        }
      case _ => None
    }
    case _ => None
  }
}
