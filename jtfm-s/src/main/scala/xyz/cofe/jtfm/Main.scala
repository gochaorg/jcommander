package xyz.cofe.jtfm

import xyz.cofe.files.AppHome
import xyz.cofe.term.common.Console
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.geom._
import xyz.cofe.jtfm.ui.table.DirectoryTable
import xyz.cofe.term.common.Position
import java.nio.file.Path
import xyz.cofe.term.common.KeyName
import xyz.cofe.jtfm.conf.UiConf
import xyz.cofe.files.FilesLogger
import org.slf4j.LoggerFactory
import xyz.cofe.term.ui.table.Column
import xyz.cofe.jtfm.ui.table.FilesTable
import xyz.cofe.term.ui.conf.MenuColorConfig
import xyz.cofe.jtfm.conf.MainMenu
import xyz.cofe.term.ui._
import xyz.cofe.term.common.Size
import xyz.cofe.jtfm.metric.MetricConf
import org.slf4j.Logger
import xyz.cofe.log._
import xyz.cofe.term.ui.conf.DialogConf
import xyz.cofe.term.common.Color
import xyz.cofe.files._
import xyz.cofe.jtfm.ui.warn.WarnDialog
import xyz.cofe.jtfm.ui.mkdir.MkDirDialog
import xyz.cofe.jtfm.conf.LeftRightDirs
import xyz.cofe.jtfm.ui.cd.ChangeDirDialog

object Main:
  implicit object appHome extends AppHome("jtfm")
  implicit val metricConf:MetricConf = MetricConf.read.getOrElse(MetricConf.defaultConf)
  private implicit lazy val logger : Logger = LoggerFactory.getLogger("xyz.cofe.jtfm.Main")
  lazy implicit val conf : UiConf = new UiConf

  def main(args:Array[String]):Unit =
    LogPrepare.prepare
    HelloMessage.writeLog

    metricConf.run {
      ConsoleBuilder.useConsole(startSession)
      
      LeftRightDirs.write(
        LeftRightDirs(
          conf.leftRightDirs.left.merge( leftPanel ),
          conf.leftRightDirs.right.merge( rightPanel ),
        )
      )
    }

  private var mbarOpt : Option[WidgetInput] = None
  private var focusedDirTableHistory:List[DirectoryTable] = List.empty

  lazy val leftPanel  = { 
    import conf.given
    implicit val dconf = conf.leftRightDirs.left
    new DirectoryTable 
  }
  lazy val rightPanel = {
    import conf.given
    implicit val dconf = conf.leftRightDirs.right
    new DirectoryTable
  }

  def startSession( console: Console ):Unit =
    import conf.given

    leftPanel.keyStrokeMap.bind(KeyStroke.KeyEvent(KeyName.Tab,false,false,false), ()=>{ rightPanel.focus.request })
    rightPanel.keyStrokeMap.bind(KeyStroke.KeyEvent(KeyName.Tab,false,false,false), ()=>{ leftPanel.focus.request })

    leftPanel.focus.onAccept  >> { focusedDirTableHistory = (leftPanel  :: focusedDirTableHistory).take(2) }
    rightPanel.focus.onAccept >> { focusedDirTableHistory = (rightPanel :: focusedDirTableHistory).take(2) }

    Session.start(console) { implicit ses =>
      import xyz.cofe.term.ui.menuBuilder._

      val vsplitPanel = VSplitPane()
      ses.rootWidget.children.append(vsplitPanel)
      vsplitPanel.bind( ses.rootWidget ){ case (root) => (root.leftTop + (0,1), root.rightBottom).rect }
      
      vsplitPanel.leftWidget.set(Some(leftPanel))
      vsplitPanel.rightWidget.set(Some(rightPanel))

      List(leftPanel,rightPanel).foreach(_.refresh)

      ses.requestFocus(leftPanel)
      leftPanel.selection.focusedIndex.set(Some(1))

      implicit val executorBuilder: Action=>()=>Unit = { act => executorOf(act) }
      implicit val menuKs = conf.mainMenu.actionKeystrokeMap
      implicit val dirMenuBuild: (MainMenu.DirTableName,WidgetChildren[Menu])=>Unit = (dn,menuParent) => {
        implicit val mp = menuParent
        dn match
          case MainMenu.DirTableName.Left  => MainMenu.tableColumns(leftPanel,  FilesTable.allColumns)
          case MainMenu.DirTableName.Right => MainMenu.tableColumns(rightPanel, FilesTable.allColumns)
      }
      MainMenu.defaultMenu.buildMenuBar
    }

  def executorOf(action:Action)(using ses:Session, conf:UiConf): ()=>Unit =
    import conf.given
    action match
      case Action.Exit => ()=>{ ses.stop = true }
      case Action.ActivateMainMenu => ()=>{ mbarOpt.foreach { mbar => mbar.focus.request } }
      case Action.MkDir => ()=>{
        focusedDirTableHistory.headOption.foreach { dirTable =>
          dirTable.directory.get.foreach { dir =>
            MkDirDialog.open( dir ).ok.listen { _ =>
              dirTable.refresh
            }
          }
        }
      }
      case Action.ChDir => ()=>{
        focusedDirTableHistory.headOption.foreach { dirTable =>
          ChangeDirDialog.open( dirTable.directory.get ).ok.listen { chDir =>
            dirTable.directory.set(Some(chDir))
            dirTable.selection.focusedIndex.set(Some(0))
            dirTable.focus.request
          }
        }
      }

  enum Action(val name:String):
    case Exit extends Action("Exit")
    case ActivateMainMenu extends Action("Show menu")
    case MkDir extends Action("Make dir")
    case ChDir extends Action("Change dir")
