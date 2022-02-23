package xyz.cofe.jtfm

import scala.ref.WeakReference

/**
 * Свойство c владельцем
 * @param _value начальное значение
 * @param _owner владелец
 * @tparam VALUE тип свойства
 * @tparam OWNER тип владельца
 */
class OwnProperty[VALUE,OWNER]
(private var _value:VALUE, private var _owner:OWNER) extends Property[OwnProperty[VALUE,OWNER],VALUE]
{
  type LISTENER=(OwnProperty[VALUE,OWNER],VALUE,VALUE)=>Unit
  private var listeners : List[LISTENER] = List()
  private def fire( old:VALUE, cur:VALUE ) = {
    listeners.foreach { _(this,old,cur) }
  }
  
  /**
   * Чтение значения свойства
   * @return значение
   */
  def value:VALUE = _value
  
  /**
   * Обновление значение свойства
   * @param v новое значение
   */
  def value(v:VALUE):Unit = {
    val old = _value
    _value = v
    fire(old,_value)
  }
  
  /**
   * Возвращает владельца свойства
   * @return владелец
   */
  def owner:OWNER = _owner
  
  /**
   * Управление подписчиком
   * @param l подписчик
   * @return Управление
   */
  def listener( l:LISTENER ) = Listener(l, ()=>listeners, (ls)=>listeners=ls )
  
  /**
   * Подписка на уведомления о изменении значения
   * @param l подписчик
   * @return отписка от уведомлений
   */
  override def listen(l: (OwnProperty[VALUE, OWNER], VALUE, VALUE) => Unit): () => Unit = {
    listener(l).add()
    val ref = WeakReference(l)
    () => {
      ref.get.foreach { r => listener(r).remove() }
    }
  }
}
