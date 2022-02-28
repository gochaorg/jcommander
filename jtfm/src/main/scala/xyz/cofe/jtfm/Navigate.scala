package xyz.cofe.jtfm

trait LikeTree[N] {
  def parent(n:N):Option[N]
  def childrenCount(n:N):Int
  def indexOf(parent:N, child:N):Option[Int]
  def child(parent:N, idx:Int):Option[N]
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
}

trait NavigateFilter[-N] {
  def test(n:N):Boolean = true
}

object NavigateFilter {
  implicit val any : NavigateFilter[Any] = new NavigateFilter[Any] {
    override def test(n: Any): Boolean = true
  }
}

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
    case class SibIter( private var from:Option[N], offset:Int=1 ) extends Iterator[N] {
      override def hasNext:Boolean = from.isDefined
      override def next(): N = {
        val r = from
        from = from.get.sib(offset)
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
      private var itr = from.iterator
      override def hasNext:Boolean = itr.hasNext
      override def next(): N = {
        val res = itr.next
        if( !itr.hasNext ){
          nextIter(res) match {
            case Some(nxt) => itr = nxt.iterator
            case _ =>
          }
        }
        res
      }
    }
  }

  def deepOrder[N](using likeTree:LikeTree[N])(using filter:NavigateFilter[N] ):Navigate[N] = new {
    def next( n:N ):Option[N] = {
      val chlds = n.children.filter( filter.test ).headOption

      val sib_i = n.siblings.filter( filter.test ).headOption      
      val sib2prnt = combine( 
        sib_i, 
        last => last.parent.map { prnt => prnt.siblings.filter(filter.test) } 
      ).headOption

      chlds.orElse( sib2prnt )      
    }
    
    private def last_child_or_self_visible(n:N):Option[N] = {
      if( !filter.test(n) ){
        None
      }else{
        var stop = false
        var from = n
        var res = Some(from)
        while( !stop ){
          val c = from.childrenCount
          if( c<1 ){
            stop = true
            res = Some(from)
          }else{
            var stop1 = false
            for( i <- (c-1) to 0 by -1 ) {
              if (!stop1) {
                from.child(i) match {
                  case Some(ch) =>
                    if( filter.test(ch) ){
                      val n_cs = ch.childrenCount
                      if( n_cs<1 ){
                        res = Some(ch)
                        stop = true
                        stop1 = true
                      }else{
                        from = ch
                      }
                    }
                  case _ =>
                }
              }
            }
          }
        }
        res
      }
    }
    def prev( n:N ):Option[N] = {
      n.sib(-1) match {
        case Some(sib) =>
          last_child_or_self_visible(sib)
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