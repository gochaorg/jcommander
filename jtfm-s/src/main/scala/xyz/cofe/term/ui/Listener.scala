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
