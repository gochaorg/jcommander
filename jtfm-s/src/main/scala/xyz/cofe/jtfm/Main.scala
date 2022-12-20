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
    
    val console = ConsoleBuilder.defaultConsole()
    Session.start(console) {
      Session.currentSession.foreach { ses => 
        val label = new Label with WidgetInput {
          def input(inputEvent: InputEvent): Unit = {
            inputEvent match
              case cEv: InputCharEvent =>
                text.set( s"Char '${cEv.getChar()}'" )
                if cEv.getChar()=='q' then ses.stop = true
              case kEv: InputKeyEvent =>
                text.set( s"Key ${kEv.getKey()}" )
                if kEv.getKey() == KeyName.Escape then ses.stop = true
              case _ => 
          }
        }
        label.location.set(Position(1,1))
        label.size.set(Size(20,1))
        label.foregroundColor.set(Color.WhiteBright)
        label.backgroundColor.set(Color.BlackBright)

        ses.rootWidget.children.append(label)
        ses.rootWidget.backgroundColor.set(Color.Green)
      }
    }

    console.close()