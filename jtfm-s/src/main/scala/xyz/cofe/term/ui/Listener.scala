package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReleaseListener

class Listener {
  var listeners = List[()=>Unit]()
  def apply( listener: => Unit ):ReleaseListener =
    val ls = ()=>{ listener }
    listeners = ls :: listeners
    ReleaseListener {
      listeners = listeners.filterNot( _ == ls )
    }

  def emit():Unit = {
    listeners.foreach( l => l() )
  }
}

object Listener:
  class ListenersWithParam[A]:
    var listeners = List[A=>Unit]()
    def emit(a:A):Unit = {
      listeners.foreach( l => l(a) )
    }
    def listen( listener: A => Unit ):ReleaseListener =
      listeners = listener :: listeners
      ReleaseListener {
        listeners = listeners.filterNot( _ == listener )
      }

  def paramter[A] = new ListenersWithParam[A]()