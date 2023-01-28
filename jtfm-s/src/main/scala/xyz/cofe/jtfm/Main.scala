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

object Main:
  implicit object appHome extends AppHome("jtfm")

  def main(args:Array[String]):Unit =
    val console = ConsoleBuilder.defaultConsole()
    startSession(console)
    console.close()

  def startSession( console: Console ):Unit =
    Session.start(console) { implicit ses =>
      import xyz.cofe.term.ui.menuBuilder._

      val leftPanel  = DirectoryTable()
      //ses.rootWidget.children.append(leftPanel)

      val rightPanel = DirectoryTable()
      //ses.rootWidget.children.append(rightPanel)

      val vsplitPanel = VSplitPane()
      ses.rootWidget.children.append(vsplitPanel)
      vsplitPanel.bind( ses.rootWidget ){ case (root) => (root.leftTop + (0,1), root.rightBottom).rect }
      
      vsplitPanel.leftWidget.set(Some(leftPanel))
      vsplitPanel.rightWidget.set(Some(rightPanel))

      //leftPanel.bind(ses.rootWidget) { case (root) => (root.leftTop + (0,1), Position(root.center.x, root.rightBottom.y)).rect }
      //rightPanel.bind(ses.rootWidget, leftPanel) { case (root,lft) => (lft.leftTop,root.rightBottom).rect }

      menuBar {
        menu("File") {
          action("Exit").exec( executorOf(Action.Exit) )
        }
      }
    }

  def executorOf(action:Action)(using ses:Session): ()=>Unit =
    action match
      case Action.Exit => ()=>{ ses.stop = true }

  enum Action:
    case Exit

// class Main(
//   ses: Session
// ):
