package xyz.cofe.jtfm

import xyz.cofe.term.common.ConsoleBuilder
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.InputCharEvent
import xyz.cofe.term.common.KeyName

object Main:
  def main(args:Array[String]):Unit =
    println("hello")

    //System.setProperty("xyz.cofe.term.default","telnet")
    //System.setProperty("xyz.cofe.term.telnet.port","12346")
    
    val console = ConsoleBuilder.defaultConsole()
    console.setCursorPosition(0,0)
    console.write("hello, press q - for exit")

    var stop = false
    while !stop do
      val evOpt = console.read()
      if evOpt.isPresent()
      then
        val ev = evOpt.get()
        console.setCursorPosition(0,1)
        console.write(s"enter $ev")

        ev match
          case ce: InputCharEvent =>
            if ce.getChar() == 'q' then
              stop = true
          case ke: InputKeyEvent =>
            if ke.getKey()==KeyName.Escape then
              stop = true
          case _ =>
      else
        Thread.sleep(1)

    console.close()