package xyz.cofe.jtfm

import xyz.cofe.files.AppHome
import xyz.cofe.term.common.ConsoleBuilder
import xyz.cofe.term.common.Console
import xyz.cofe.term.ui.Session
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.geom._
import xyz.cofe.jtfm.ui.table.DirectoryTable
import xyz.cofe.term.common.Position
import xyz.cofe.term.ui.VSplitPane
import java.nio.file.Path
import xyz.cofe.term.ui.MenuBar
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.ui.Widget
import xyz.cofe.term.ui.WidgetInput

object Main:
  implicit object appHome extends AppHome("jtfm")

  def main(args:Array[String]):Unit =
    LogPrepare.prepare
    HelloMessage.writeLog

    val console = PrepareConsole.createConsole
    startSession(console)
    console.close()

  private var mbarOpt : Option[WidgetInput] = None
  def startSession( console: Console ):Unit =
    Session.start(console) { implicit ses =>
      import xyz.cofe.term.ui.menuBuilder._

      val leftPanel  = new DirectoryTable
      val rightPanel = new DirectoryTable

      leftPanel.directory.set(Some(Path.of(".")))
      rightPanel.directory.set(Some(Path.of(".")))

      val vsplitPanel = VSplitPane()
      ses.rootWidget.children.append(vsplitPanel)
      vsplitPanel.bind( ses.rootWidget ){ case (root) => (root.leftTop + (0,1), root.rightBottom).rect }
      
      vsplitPanel.leftWidget.set(Some(leftPanel))
      vsplitPanel.rightWidget.set(Some(rightPanel))

      ses.requestFocus(leftPanel)
      leftPanel.selection.focusedIndex.set(Some(1))

      menuBar {
        menu("File") {
          action("Exit").exec( executorOf(Action.Exit) )
        }
        mbarOpt = Some(menu("View") {
          action("Show menu").keyStroke(KeyStroke.KeyEvent(KeyName.F5,false,false,false)) {
            println("aa")
            mbarOpt.foreach { mbar => mbar.focus.request }
          }
        })
      }
    }

  def executorOf(action:Action)(using ses:Session): ()=>Unit =
    action match
      case Action.Exit => ()=>{ ses.stop = true }
      case Action.ActivateMainMenu => ()=>{}

  enum Action:
    case Exit
    case ActivateMainMenu

