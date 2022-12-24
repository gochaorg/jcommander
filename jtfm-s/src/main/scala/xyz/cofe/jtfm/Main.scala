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
import xyz.cofe.files.AppHome
import xyz.cofe.files.log.PathPattern.AppHomeProvider
import xyz.cofe.files.log.PathPattern
import java.nio.file.Path
import xyz.cofe.files.log.AppendableFile.apply
import xyz.cofe.files.log.AppendableFile
import xyz.cofe.term.ui.ses.SesInputLog
import xyz.cofe.files.log.PathPattern.Evaluate

object Main:
  object appHome extends AppHome("jtfm")
  implicit val appHomeProvider : AppHomeProvider = AppHomeProvider.provide(appHome.directory)
  implicit val pathPatternEval : Evaluate = Evaluate.defaultEvaluate

  lazy val logPathCommon = "{appHome}/log/{yyyy}/{MM}/{dd}/{HH}-{mi}-pid{pid}-"
  lazy val sesInputLogPath = PathPattern.parse(Path.of(s"${logPathCommon}sesInput.txt"))
  lazy val sesInputOut = AppendableFile(sesInputLogPath,Some(1024*1024*16))
  given sesInputLog : SesInputLog = SesInputLog.simple(sesInputOut)

  def main(args:Array[String]):Unit =
    //System.setProperty("xyz.cofe.term.default","telnet")
    //System.setProperty("xyz.cofe.term.telnet.port","12346")

    val console = ConsoleBuilder.defaultConsole()
    Session.start(console) {
      Session.currentSession.foreach { ses => 
        val label = new Label with WidgetInput {
          override def input(inputEvent: InputEvent): Boolean = {
            inputEvent match
              case cEv: InputCharEvent =>
                text.set( s"Char '${cEv.getChar()}'" )
                if cEv.getChar()=='q' then ses.stop = true
              case kEv: InputKeyEvent =>
                text.set( s"Key ${kEv.getKey()}" )
                if kEv.getKey() == KeyName.Escape then ses.stop = true
              case _ => 
            false
          }
        }
        label.location.set(Position(1,1))
        label.size.set(Size(20,1))
        label.foregroundColor.set(Color.WhiteBright)
        label.backgroundColor.set(Color.BlackBright)

        ses.rootWidget.children.append(label)

        val pnl1 = new FocPanel("pnl1")
        pnl1.location.set(Position(1,3))
        pnl1.size.set(Size(30,1))
        ses.rootWidget.children.append(pnl1)

        ses.rootWidget.backgroundColor.set(Color.Green)

        val but1 = Button("exit").action { ses.stop = true }
        but1.location.set(Position(35,2))
        ses.rootWidget.children.append(but1)

        val but2 = Button("foc on pnl1").action { ses.stop = true }
        but2.location.set(Position(35,4))
        ses.rootWidget.children.append(but2)

        val menuBar = MenuBar()
        val menuFile = MenuContainer("File")
        val menuFileExit = MenuAction("Exit")
        val menuView = MenuContainer("View")
        val menuViewSome = MenuAction("Some")

        menuBar.children.append(menuFile)        
        menuBar.children.append(menuView)
        menuFile.children.append(menuFileExit)
        menuView.children.append(menuViewSome)

        menuBar.install(ses.rootWidget)
      }
    }

    console.close()