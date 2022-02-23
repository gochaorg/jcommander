package xyz.cofe.jtfm

import org.junit.Test

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
