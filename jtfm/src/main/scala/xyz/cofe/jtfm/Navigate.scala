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

/**
 * Навигация по дереву
 */
trait Navigate[N] {
  def next( n:N ):Option[N]
  def prev( n:N ):Option[N]
  def forwardIterator( n:N ):Iterator[N] = { 
    val fetch = next
    new Iterator[N] {
      private var from : Option[N] = Some(n)
      def hasNext:Boolean = from.isDefined
      def next:N = {
        val r = from.get
        from = fetch(n)
        r
      }
    }
  }
  def backwardIterator( n:N ):Iterator[N] = { 
    val fetch = prev
    new Iterator[N] {
      private var from : Option[N] = Some(n)
      def hasNext:Boolean = from.isDefined
      def next:N = {
        val r = from.get
        from = fetch(n)
        r
      }
    }
  }
}

/**
 * Фильтр навигации по дереву
 */
trait NavigateFilter[-N] {
  def test(n:N):Boolean = true
}

object NavigateFilter {
  /**
   * Фильтр всегда возвращающий true
   */
  implicit val any : NavigateFilter[Any] = new NavigateFilter[Any] {
    override def test(n: Any): Boolean = true
  }

  /** Создание фильтра */
  def create[N]( f:N=>Boolean ):NavigateFilter[N] = new {
    override def test(n:N) = f(n)
  }
}

/**
 * Расширение для навигации
 */
implicit class LikeTreeOps[N]( val n:N )(implicit val likeTree: LikeTree[N] ) {
  def parent:Option[N] = likeTree.parent(n)
  def childrenCount = likeTree.childrenCount(n)
  def indexOf(child:N) = likeTree.indexOf(n,child)
  def child(idx:Int) = likeTree.child(n, idx)
  def sib(idx:Int) = likeTree.sib(n, idx)
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
    object reverse extends Iterable[N] {
      def iterator:Iterator[N] = SibIter(n.sib(-1),-1)
    }
  }
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

object Navigate {
  def combine[N]( from:Iterable[N], nextIter:N=>Option[Iterable[N]] ):Iterable[N] = new Iterable[N] {
    def iterator:Iterator[N] = new {
      private def debug(s:String) = ()
      private def debugln(s:String) = ()
      private var itr = from.iterator
      override def hasNext:Boolean = itr.hasNext
      override def next(): N = {
        debug("combine ");
        val res = itr.next
        debug(s"res=$res ")
        if( !itr.hasNext ){
          debug("tryNext ")
          nextIter(res) match {
            case Some(nxt) =>
              debug(" accept")
              itr = nxt.iterator
            case _ =>
              debug(" no")
          }
        }
        debugln("")
        res
      }
    }
  }

  def deepOrder[N](using likeTree:LikeTree[N])(using filter:NavigateFilter[N] ):Navigate[N] = new {
    private def next_sibPrnt( from:N ):Option[N] = from.parent match {
      case Some(prnt) =>
        prnt.siblings.filter( filter.test ).headOption match {
          case Some(sibPrnt) =>
            Some(sibPrnt)
          case None =>
            next_sibPrnt(prnt)
        }
      case None => None
    }
    
    def next( n:N ):Option[N] = {
      n.children.filter( filter.test ).headOption match {
        case Some(ch) => Some(ch)
        case None =>
          next_sibPrnt(n).headOption match {
            case Some(ch) => Some(ch)
            case None => n.siblings.filter( filter.test ).headOption
          }
      }
    }
  
    private def last_child_or_self_visible1(n:N):Option[N] = {
      if( !filter.test(n) ){
        None
      }else{
        if( n.childrenCount<1 ){
          Some(n)
        }else {
          val from = n.children.reverse
            .filter { filter.test }
            .headOption
          
          combine(
            from, visible =>
              if( visible.childrenCount<1 ) {
                Some(List(visible))
              } else {
                Some(visible.children.reverse.headOption)
              }
          ).headOption
        }
      }
    }
    
    def prev( n:N ):Option[N] = {
      n.sib(-1) match {
        case Some(sib) =>
          val r1 = last_child_or_self_visible1(sib)
          r1
        case _ =>
          n.parent match {
            case Some(prt) if filter.test(prt) =>
              Some(prt)
            case _ =>
              None
          }
      }
    }
  }
}