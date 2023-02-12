package xyz.cofe.jtfm.ui

import xyz.cofe.term.ui.Listener

class Promise[A]():
  val ok: Listener[A] = Listener()
  val closed: Listener[Unit] = Listener.unit

