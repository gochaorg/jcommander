package xyz.cofe.jtfm.ev

import scala.ref.WeakReference

class EvalProperty[VALUE,SELF]
(
  val compute: ()=>VALUE,
  val initial: Option[VALUE]=None,
  val changeNotify: Option[ (()=>Unit)=>Unit ]=None
)
extends Property[EvalProperty[VALUE,SELF],VALUE]
{
  private var currentValue:Option[VALUE] = initial
  
  /**
   * Чтение значения свойства
   *
   * @return значение
   */
  override def value: VALUE = {
    currentValue match {
      case Some(cv) => cv
      case None =>
        val v = compute()
        currentValue = Some(v)
        v
    }
  }
  
  type LISTENER=(EvalProperty[VALUE,SELF],VALUE,VALUE)=>Unit
  
  private var listeners : List[LISTENER] = List()
  protected def fire( old:VALUE, cur:VALUE ) = {
    listeners.foreach { _(this,old,cur) }
  }
  
  protected def recompute():Unit = currentValue match {
    case Some(old) =>
      val newVal = compute()
      currentValue = Some(newVal)
      fire(old, newVal)
    case None =>
      val newVal = compute()
      currentValue = Some(newVal)
  }
  
  changeNotify match {
    case Some(notif) =>
      val doFire: ()=>Unit = recompute
      notif(doFire)
    case None =>
  }
  
  /**
   * Управление подписчиком
   * @param l подписчик
   * @return Управление
   */
  def listener( l:LISTENER ) = Listener(l, ()=>listeners, (ls)=>listeners=ls )
  
  /**
   * Подписка на уведомления о изменении значения
   *
   * @param l подписчик
   * @return отписка от уведомлений
   */
  override def listen(l: (EvalProperty[VALUE,SELF], VALUE, VALUE) => Unit): () => Unit = {
    listener(l).add()
    val ref = WeakReference(l)
    () => {
      ref.get.foreach { r => listener(r).remove() }
    }
  }
}
