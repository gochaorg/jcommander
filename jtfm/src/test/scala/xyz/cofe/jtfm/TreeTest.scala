package xyz.cofe.jtfm

import org.junit.Test
import xyz.cofe.jtfm.ev.{BasicCollection, CollectionWithNotify, OwnProperty}
import xyz.cofe.jtfm.wid.Widget

class TreeTest {
  class Wid(val text:String) extends Widget[Wid] {
    override def toString: String = s"$text"
  }
  
  @Test
  def test01():Unit = {
    val w1 = Wid("w1")
    val w2 = Wid("w2")
    val w3 = Wid("w3")
    val w4 = Wid("w4")
    w1.nested.append(w2)
    w1.nested.append(w3)
    w3.nested.append(w4)
    
    println( s"w1 parent ${w1.parent.value}" )
    println( s"w2 parent ${w2.parent.value}" )
    println( s"w3 parent ${w3.parent.value}" )
    println( s"w4 parent ${w4.parent.value}" )
    
    val p = w4.widgetPath
    println(p)
    
    import Widget._
    import NavigateFilter.any
    val nav = Navigate.deepOrder
    
    Map(
      w1->(Some(w2),None),
      w2->(Some(w3),Some(w1)),
      w3->(Some(w4),Some(w2)),
      w4->(None,Some(w3)),
    ).foreach { case (from, must) =>
      val (nxt,prv) = must
      val nxt_w = nav.next(from)
      val prv_w = nav.prev(from)
      val nxt_m = nxt_w == nxt
      val prv_m = prv_w == prv
      println(s"from $from next:$nxt_w match $nxt_m;   prev:$prv_w match $prv_m")
      assert(nxt_m)
      assert(prv_m)
    }
  }
}
