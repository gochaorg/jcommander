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
    
    List(
      w1,w2,w3,
      w4
    ).foreach { w =>
      val next = nav.next(w)
      val prev = nav.prev(w)
      println(s"${w} next:${next} prev:${prev}")
    }
  }
}
