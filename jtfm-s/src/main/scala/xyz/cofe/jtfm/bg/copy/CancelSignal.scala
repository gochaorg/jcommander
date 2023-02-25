package xyz.cofe.jtfm.bg.copy

trait CancelSignal:
  def listen( listener: =>Unit ):Unit
  def send():Unit

object CancelSignal:
  class CancelSignalImpl extends CancelSignal:
    @volatile var listeners:List[()=>Unit] = List.empty
    override def listen(listener: => Unit): Unit = 
      listeners = listeners :+ ( ()=>{listener} )
    override def send(): Unit = 
      listeners.foreach(ls => ls())