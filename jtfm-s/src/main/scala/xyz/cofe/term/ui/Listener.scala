package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReleaseListener

class Listener[A] {
  var listeners = List[A=>Unit]()
  def listen( listener: A => Unit ):ReleaseListener =
    listeners = listener :: listeners
    ReleaseListener {
      listeners = listeners.filterNot( _ == listener )
    }

  def emit(a:A):Unit = {
    listeners.foreach( l => l(a) )
  }
}

object Listener:
  def unit:Listener[Unit] = new Listener[Unit]

extension (listener:Listener[Unit])
  def listen( handler: =>Any ):ReleaseListener =
    listener.listen( _ => handler )
  def emit():Unit =
    listener.emit(())

extension [A](listener:Listener[A])
  def >>( handler: =>Any ):ReleaseListener =
    listener.listen( _ => handler )