package xyz.cofe.jtfm

import com.googlecode.lanterna.terminal.Terminal

class Session ( terminal: Terminal ):
  println(s"start session $terminal")
  terminal.close  

  def terminate():Unit =
    println(s"terminate")
    terminal.close()
