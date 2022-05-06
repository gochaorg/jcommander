package xyz.cofe.jtfm.ev

import org.junit.Test
import xyz.cofe.jtfm.ev.BasicCollection

class BasicCollTest {
  @Test
  def test01():Unit = {
    val coll = new BasicCollection[String]
    coll.listen( (cl,i,old,cur) => {
      println(s"event [${i}] $old => $cur")
    })
    coll.append("abc")
    coll.append("bcd")
    coll.prepend("xyz")
    coll.set(0,"yyy")
  }
}
