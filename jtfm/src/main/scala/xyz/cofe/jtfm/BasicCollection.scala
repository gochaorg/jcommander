package xyz.cofe.jtfm

import scala.ref.WeakReference

class BasicCollection[N]
  extends Collection[N]
    with MutCollection[N]
    with CollectionWithNotify[BasicCollection[N],N]
{
  private var list:List[N] = List()
  private var listeners:List[LISTENER] = List()
  
  override def size: Int = list.size
  override def apply(idx: Int): N = list(idx)
  
  override def append(n: N): Unit = {
    list = list ::: List(n)
    inserted(list.size-1,n)
  }
  override def prepend(n: N): Unit = {
    list = n :: list
    inserted(0,n)
  }
  override def removeAt(idx: Int): Unit = {
    var what = List[(Int,N)]()
    list = list.zip(0 until list.size).filter((n,ni)=>{
      if( ni==idx ){
        what = (ni,n) :: what
        false
      }else{
        true
      }
    }).map(_._1)
    what.foreach( x => deleted(x._1, x._2) )
  }
  override def set(idx: Int, n: N): Unit = {
    var what = List[(Int,N,N)]()
    list = list.zip(0 until list.size).
      map( (e,ei) => if(ei==idx){
        what = (ei,e,n) :: what
        n
      }else{
        e
      })
    what.foreach(updated)
  }
  
  /**
   * Управление подписчиком
   * @param l подписчик
   * @return Управление
   */
  def listener( l:LISTENER ) = Listener(l, ()=>listeners, (ls)=>listeners=ls )
  override def listen(l: LISTENER): () => Unit = {
    listener(l).add()
    val ref = WeakReference(l)
    () => {
      ref.get.foreach { r => listener(r).remove() }
    }
  }
  
  private def fire( idx:Int, old:Option[N], cur:Option[N] ) = listeners.foreach { _(this,idx,old,cur) }
  private def inserted( idx:Int, itm:N ) = fire(idx,None,Some(itm))
  private def deleted( idx:Int, itm:N ) = fire(idx,Some(itm),None)
  private def updated( idx:Int, old:N, cur:N ) = fire(idx,Some(old),Some(cur))
  
  override def iterator: Iterator[N] = list.iterator
}
