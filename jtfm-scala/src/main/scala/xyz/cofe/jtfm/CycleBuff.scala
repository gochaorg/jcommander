package xyz.cofe.jtfm

import scala.reflect.ClassTag

class CycleBuff[A:ClassTag]( val limit:Int ) {
  require(limit>0)

  private var data0:Array[A] = new Array[A](limit);

  private var ptr:Long = 0
  def pointer:Long = ptr

  def size:Int = (ptr min data0.length).toInt

  def push(a:A):Unit = {
    val pIdx = (ptr % data0.length).toInt
    data0(pIdx) = a
    ptr += 1
  }

  def values:Seq[A]=
    if ptr<data0.length then
      Array.copyOf(data0,ptr.toInt)
    else
      val x = (ptr % data0.length).toInt
      if x==0 then
        data0
      else
        val right = new Array[A]( data0.length - x )
        val left  = new Array[A]( x )
        Array.copy(data0,x,right,0,right.length)
        Array.copy(data0,0,left,0,x)
        Array.concat(right,left)
}
