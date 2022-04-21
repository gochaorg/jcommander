package xyz.cofe.jtfm.ev

import org.junit.Test

class PropTest {
  @Test
  def test01():Unit = {
    import Prop._

    val a = Prop.writeable(10).withName("a")
    val b = Prop.writeable(12).withName("b")
    val c = Prop.compute[Int] { a + b + b }

    println( c.get )
    println( c.refs )
  }
}
