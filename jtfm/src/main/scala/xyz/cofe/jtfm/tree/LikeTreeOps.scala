package xyz.cofe.jtfm.tree

import xyz.cofe.jtfm.LikeTree

/**
 * Расширение для навигации
 */
implicit class LikeTreeOps[N]( val n:N )(implicit val likeTree: LikeTree[N] ) {
  /**
   * Возвращает родительский узел
   * @return родительский узел
   */
  def parent:Option[N] = likeTree.parent(n)
  
  /**
   * Возвращает кол-во дочерних узлов
   * @return кол-во дочерних узлов
   */
  def childrenCount = likeTree.childrenCount(n)
  
  /**
   * Возвращает индекс дочернего узла
   * @param child узел
   * @return индекс
   */
  def indexOf(child:N) = likeTree.indexOf(n,child)
  
  /**
   * Возвращает дочерний узел по его индексу
   * @param idx индекс
   * @return узел
   */
  def child(idx:Int) = likeTree.child(n, idx)
  
  /**
   * Возвращает соседний (брат/сестра) узел относительно текущего
   * @param idx индекс, -1 - соседний слева, +1 - соседний справа
   * @return соседний узел
   */
  def sib(idx:Int) = likeTree.sib(n, idx)
  
  /**
   * Итератор по дочерним узлам
   */
  object children extends Iterable[N] {
    override def size:Int = likeTree.childrenCount(n)    
    def iterator:Iterator[N] = new Iterator[N] 
    {
      private var idx:Int = 0
      private var cnt:Int = likeTree.childrenCount(n)
      override def hasNext: Boolean = idx < cnt
      override def next(): N = {
        val c = child(idx)
        idx += 1
        c.get
      }
    }
  
    /**
     * Обратный итератор, от последнего дочернего, к первому дочернему
     */
    object reverse extends Iterable[N] {
      def iterator:Iterator[N] = new Iterator[N] {
        private var idx:Int = likeTree.childrenCount(n) - 1
        override def hasNext: Boolean = idx >= 0
        override def next(): N = {
          val c = child(idx)
          idx -= 1
          c.get
        }
      }
    }
  }
  
  /**
   * Итератор по соседним узлам, от текущего.
   * Сам узел не будет включен в список итерируемых.
   */
  object siblings extends Iterable[N] {
    private def debug(s:String) =
      ()//println(s)
      
    case class SibIter( private var from:Option[N], offset:Int=1 ) extends Iterator[N] {
      private val from_init = from
      override def hasNext:Boolean = from.isDefined
      override def next(): N = {
        val r = from
        from = from.get.sib(offset)
        debug(
          s"""|sib next(from=$from_init offset=$offset):
              |  res: $r
              |  follow: $from
              """.stripMargin.trim)
        r.get
      }
    }
    def iterator:Iterator[N] = SibIter(n.sib(1),1)
  
    /**
     * Обратный итератор.
     * Сам узел не будет включен в список итерируемых.
     */
    object reverse extends Iterable[N] {
      def iterator:Iterator[N] = SibIter(n.sib(-1),-1)
    }
  }
  
  /**
   * Итератор по родительским узлам, от текущего к корню
   * Сам узел не будет включен в список итерируемых.
   */
  object parents extends Iterable[N] {
    def iterator:Iterator[N] = new {
      private var from = parent
      override def hasNext:Boolean = from.isDefined
      override def next(): N = {
        var r = from
        from = from.get.parent
        r.get
      }
    }
  }
}