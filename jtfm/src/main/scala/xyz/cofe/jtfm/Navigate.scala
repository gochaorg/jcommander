package xyz.cofe.jtfm

import tree._

/**
 * Навигация по дереву
 */
trait Navigate[N] {
  /**
   * Навигация к следующему узлу
   * @param n узел
   * @return следующий узел
   */
  def next( n:N ):Option[N]
  
  /**
   * Навигация к предыдущему узлу
   * @param n узел
   * @return предыдущий узел
   */
  def prev( n:N ):Option[N]
  
  /**
   * Итератор вперед (next) от указанного
   * @param n от какого узла навигация
   * @return итератор, включая указанный
   */
  def forwardIterator( n:N ):Iterator[N] = { 
    val fetch = next
    new Iterator[N] {
      private var from : Option[N] = Some(n)
      def hasNext:Boolean = from.isDefined
      def next:N = {
        val r = from.get
        from = fetch(r)
        r
      }
    }
  }
  
  /**
   * Итератор назад (prev) от указанного
   * @param n от какого узла навигация
   * @return итератор, включая указанный
   */
  def backwardIterator( n:N ):Iterator[N] = { 
    val fetch = prev
    new Iterator[N] {
      private var from : Option[N] = Some(n)
      def hasNext:Boolean = from.isDefined
      def next:N = {
        val r = from.get
        from = fetch(r)
        r
      }
    }
  }
  
  /**
   * Последний элемент
   * @param from от какого узла навигация
   * @return Последний элемент
   */
  def last( from:N ):Option[N]
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
          n.siblings.filter(filter.test).headOption.orElse(
            next_sibPrnt(n)
          )
//
//          next_sibPrnt(n).headOption match {
//            case Some(ch) => Some(ch)
//            case None => n.siblings.filter( filter.test ).headOption
//          }
      }
    }
  
    private def last_child_or_self_visible1(n:N):Option[N] = {
      if( !filter.test(n) ){
        //println(s"last_child_or_self_visible1#filter => None")
        None
      }else{
        if( n.childrenCount<1 ){
          //println(s"last_child_or_self_visible1#Some($n) - no child")
          Some(n)
        }else {
          val from = n.children.reverse
            .filter { filter.test }
            .headOption
          
          val x = combine(
            from, visible =>
              if( visible.childrenCount<1 ) {
                Some(List(visible))
              } else {
                Some(visible.children.reverse.headOption)
              }
          ).headOption

          val xx = x.orElse( Some(n) )
          //println(s"last_child_or_self_visible1#xx = $xx")
          xx
        }
      }
    }
    
    def prev( n:N ):Option[N] = {
      //println(s"prev($n)")
      n.sib(-1) match {
        case Some(sib) =>
          val r1 = last_child_or_self_visible1(sib)
          //println(s"prev#last_child_or_self_visible1($sib)=$r1")
          r1
        case _ =>
          n.parent match {
            case Some(prt) if filter.test(prt) =>
              //println(s"prev#prnt($prt)")
              Some(prt)
            case _ =>
              //println(s"prev#none")
              None
          }
      }
    }
    
    def last( from:N ):Option[N] = {
      var n = from
      var stop = false
      while( !stop ){
        val cc = n.childrenCount
        if( cc>0 ){
          n.child(cc-1) match {
            case Some(nc) => n = nc
            case None => stop = true
          }
        }else{
          stop = true
        }
      }
      Some(n)
    }
  }
}