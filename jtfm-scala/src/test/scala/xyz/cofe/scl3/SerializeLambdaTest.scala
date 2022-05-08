package xyz.cofe.scl3

import org.junit.Test
import java.lang.invoke.SerializedLambda

class SerializeLambdaTest {
  def double( a:Int ):()=>Int = ()=>a+a

  def capute( some:()=>Int ):Unit = {
    // some.getClass.getDeclaredMethods.foreach { meth =>
    //   println( meth )
    // }
    val meth = some.getClass.getDeclaredMethod( "writeReplace" )
    meth.setAccessible(true)
    val sl = meth.invoke(some).asInstanceOf[SerializedLambda]
    (0 until sl.getCapturedArgCount).foreach { ai => 
      println( sl.getCapturedArg(ai) )
    }
  }

  @Test
  def test01():Unit={
    val l1 = double(2)
    println(l1)
    capute(l1)
  }
}
