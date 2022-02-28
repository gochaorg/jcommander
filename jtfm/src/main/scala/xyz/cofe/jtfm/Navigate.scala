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
}

object Navigate {
  def deepOrder[N](using likeTree:LikeTree[N])(using filter:NavigateFilter[N] ):Navigate[N] = new {
    def next( n:N ):Option[N] = {
      val childCnt = n.childrenCount
      var res:Option[N] = None;
      if( childCnt>0 ){
        (0 until childCnt).foreach { i =>
          res match {
            case None =>
              n.child(i) match {
                case Some(ch) =>
                  if( filter.test(ch) ){
                    res = Some(ch)
                  }
                case _ =>
              }
            case Some(_) =>
          }
        }
      }
      res match {
        case None =>
          var from = n;
          var br_p = false
          while( res.isEmpty && !br_p ){
            var br = false
            while( !br ){
              from.sib(1) match {
                case Some(sib) =>
                  if( filter.test(sib) ){
                    br = true
                    res = Some(sib)
                  }else{
                    from = sib
                  }
                case None =>
                  br = true
              }
            }
            from.parent match {
              case Some(prnt) => from = prnt
              case _ => br_p = true
            }
          }
        case Some(_) =>
      }
      res
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