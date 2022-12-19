package xyz.cofe.jtfm

import xyz.cofe.term.common.ConsoleBuilder
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.InputCharEvent
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.ui.Session
import xyz.cofe.term.ui.Label
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.ui.WidgetInput
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.Color
import xyz.cofe.term.ui
import xyz.cofe.term.ui._
import xyz.cofe.term.buff.ScreenChar

object Main:
  def main(args:Array[String]):Unit =
    //System.setProperty("xyz.cofe.term.default","telnet")
    //System.setProperty("xyz.cofe.term.telnet.port","12346")
    
    def dump()= {
      def col(col:Color) =
        col match
          case Color.WhiteBright => "a"
          case Color.Black => "0"
          case Color.BlackBright => "b"
          case Color.Red => "1"
          case Color.RedBright => "c"
          case Color.Green => "2"
          case Color.GreenBright => "d"
          case Color.Yellow => "3"
          case Color.YellowBright => "e"
          case Color.Blue => "4"
          case Color.BlueBright => "f"
          case Color.Magenta => "5"
          case Color.MagentaBright => "g"
          case Color.Cyan => "6"
          case Color.CyanBright => "h"
          case Color.White => "7"
        
      def str(chr:ScreenChar) = 
        chr.char + col(chr.foreground) + col(chr.background)

      Session.currentSession.foreach { ses =>
        val buf = ses.screenBuffer
        println(s"size ${buf.width} x ${buf.height}")
        (0 until buf.height).foreach { y =>
          print(y.toString().padTo(4,' '))
          println( (0 until buf.width).map { x => 
              buf.get(x,y).map(str).getOrElse("?").padTo(3,' ')
            }.mkString("")
          )
        }
      }
    }

    val console = ConsoleBuilder.defaultConsole()
    Session.start(console) {
      Session.currentSession.foreach { ses => 
        val label = new Label with WidgetInput {
          def input(inputEvent: InputEvent): Unit = {
            inputEvent match
              case cEv: InputCharEvent =>
                text.set( s"Char '${cEv.getChar()}'" )
                if cEv.getChar()=='q' then ses.stop = true
                if cEv.getChar()=='z' then dump()
              case kEv: InputKeyEvent =>
                text.set( s"Key ${kEv.getKey()}" )
                if kEv.getKey() == KeyName.Escape then ses.stop = true
              case _ => 
          }
        }
        label.location.set(Position(1,1))
        label.size.set(Size(20,1))
        label.foregroundColor.set(Color.WhiteBright)
        //label.backgroundColor.set(Color.BlackBright)

        ses.rootWidget.children.append(label)
        ses.rootWidget.backgroundColor.set(Color.Green)
      }
    }

    // console.setCursorPosition(0,0)
    // console.write("hello, press q - for exit")

    // var stop = false
    // while !stop do
    //   val evOpt = console.read()
    //   if evOpt.isPresent()
    //   then
    //     val ev = evOpt.get()
    //     console.setCursorPosition(0,1)
    //     console.write(s"enter $ev")

    //     ev match
    //       case ce: InputCharEvent =>
    //         if ce.getChar() == 'q' then
    //           stop = true
    //       case ke: InputKeyEvent =>
    //         if ke.getKey()==KeyName.Escape then
    //           stop = true
    //       case _ =>
    //   else
    //     Thread.sleep(1)

    console.close()