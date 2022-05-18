package xyz.cofe.jtfm

import org.junit.Test

class CycleBuffTest {
  @Test
  def test01():Unit =
    val buff = CycleBuff[Int](5)
    (0 until 20).foreach { idx =>
      buff.push(idx)
      println( s"[$idx] size=${buff.size} ptr=${buff.pointer} ${buff.values}" )
    }
}
