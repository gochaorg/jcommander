package xyz.cofe.jtfm

import com.googlecode.lanterna.terminal.Terminal
import xyz.cofe.jtfm.wid.{Widget, WidgetCycle}

import java.util.concurrent.atomic.AtomicReference

class Session ( terminal: Terminal ):
  private val wc_atom : AtomicReference[WidgetCycle] = new AtomicReference[WidgetCycle](null)
  
  def run():Unit =
    println(s"session is started")
    WidgetCycle(terminal) match {
      case Left(err) =>
        println(s"fail $err")
      case Right(wc) =>
        wc_atom.set(wc)
        wc.run()
        println(s"session is stopped")
    }

  def terminate():Unit =
    val wc = wc_atom.get()
    if wc!=null then
      wc.stop().await()
