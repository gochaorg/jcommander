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
    val w5 = Wid("w5")
    val w6 = Wid("w6")
    val w7 = Wid("w7")
    w1.nested.append(w2)
    w1.nested.append(w3)
    w3.nested.append(w4)
    w1.nested.append(w5)
    w5.nested.append(w6)
    w5.nested.append(w7)
    
    println(
      s"""|w3:
          |  ${ w3.nested }""".stripMargin
    )
    
    val p = w4.widgetPath
    println(p)
    
    import Widget._
    import NavigateFilter.any
    val nav = Navigate.deepOrder
    
    println(
      s"""|w4:
          |  parent: ${w4.parent.value}
          |  children: ${w4.nested}""".stripMargin)
    println("try nextttxxx")
    nav.next(w4)
    
    var failCount = 0
    
    List(
      w1->(Some(w2),None),
      w2->(Some(w3),Some(w1)),
      w3->(Some(w4),Some(w2)),
      w4->(Some(w5),Some(w3)),
      w5->(Some(w6),Some(w4)),
      w6->(Some(w7),Some(w5)),
      w7->(None,Some(w6)),
    ).foreach { case (from, must) =>
      val (nxt,prv) = must
      val nxt_w = nav.next(from)
      val prv_w = nav.prev(from)
      val nxt_m = nxt_w == nxt
      val prv_m = prv_w == prv
      println("path "+from.widgetPath.map(_.toString).foldLeft("")((a,b)=>a+"/"+b))
      println(
        s"""|  from $from
            |     next ${nxt_w.toString()} match $nxt_m
            |     prev $prv_w match $prv_m""".stripMargin
      )
      if (!nxt_m) { failCount+=1 }
      if (!prv_m) { failCount+=1 }
    }
    
    println(s"fail count $failCount")
    assert(failCount<1)
    
    println("-_".repeat(40))
    println("fw iter from w1")
    val fItr = nav.forwardIterator(w1)
    
    val fEl0 = fItr.next()
    println(s"next(0) = ${fEl0}")
    
    val fEl1 = fItr.next()
    println(s"next(1) = ${fEl1}")
    
    val fEl2 = fItr.next()
    println(s"next(2) = ${fEl2}")
    
    val fEl3 = fItr.next()
    println(s"next(3) = ${fEl3}")
  }
}
