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
import scala.collection.immutable.LazyList.cons
import xyz.cofe.term.cs._

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
        val pnl1 = new FocPanel("pnl1")
        pnl1.location.set(Position(1,3))
        pnl1.size.set(Size(30,1))
        ses.rootWidget.children.append(pnl1)

        ses.rootWidget.backgroundColor.set(Color.Green)

        val but1 = Button("exit").action { ses.stop = true }
        but1.location.set(Position(35,2))
        ses.rootWidget.children.append(but1)

        val but2 = Button("foc on pnl1").action { pnl1.focus.request }
        but2.location.set(Position(35,4))
        ses.rootWidget.children.append(but2)

        val but3 = Button("clear buff").action {
          (0 until ses.screenBuffer.height).flatMap { y => 
            (0 until ses.screenBuffer.width).map { x => (x,y) }
          }.foreach { case (x,y) => 
            ses.screenBuffer.set(x,y,ScreenChar(' ',Color.White,Color.Black))
          }
        }
        but3.location.set(Position(35,6))
        ses.rootWidget.children.append(but3)

        val but4 = Button("clear console").action {
          console.setBackground(Color.Black)
          console.setForeground(Color.White)
          (0 until ses.screenBuffer.height).flatMap { y => 
            (0 until ses.screenBuffer.width).map { x => (x,y) }
          }.foreach { case (x,y) => 
            console.setCursorPosition(x,y)
            console.write(" ")
          }
        }
        but4.location.set(Position(35,8))
        ses.rootWidget.children.append(but4)

        val but5 = Button("dump").action {
          ses.rootWidget.walk.path.foreach { path =>
            print("  "*path.rpath.size)
            val v = path.node match
              case vp:VisibleProp => s"${vp.visible.value.get}"
              case _ => "?"
            
            println(s"${path.node} v=${v} ${path.node.location.get} ${path.node.size.get}")
          }
        }
        but5.location.set(Position(35,10))
        ses.rootWidget.children.append(but5)

        val menuBar = MenuBar()
        val menuFile = MenuContainer("File")
        
        val menuFileOpen = 
          MenuAction("Open")
            .action { println("open") }
            .keyStroke( KeyStroke.KeyEvent(KeyName.F2,false,false,false) )

        val menuFileExit = MenuAction("Exit").action { println("exit"); ses.stop = true }        
        val menuFileSome = MenuAction("Some else")
        val menuView = MenuContainer("View")
        val menuViewSome = MenuAction("Some")

        val menuViewSub = MenuContainer("Sub")
        menuView.children.append(menuViewSub)
        (0 until 15).foreach { i => 
          val miSub = MenuAction(s"sub $i")
          menuViewSub.children.append(miSub)
        }

        menuFileExit.size = Size(15,1)

        menuBar.children.append(menuFile)        
        menuBar.children.append(menuView)
        menuFile.children.append(menuFileOpen)
        menuFile.children.append(menuFileExit)
        menuFile.children.append(menuFileSome)
        menuView.children.append(menuViewSome)

        (0 until 15).foreach { i => 
          val mi = MenuAction(s"menu $i")
          menuFile.children.append(mi)
        }

        menuBar.install(ses.rootWidget)
      }
    }

    console.close()