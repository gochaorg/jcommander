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
          leftPanel.updateConf(conf.leftRightDirs.left),
          rightPanel.updateConf(conf.leftRightDirs.right)
        )
      )
    }

  private var mbarOpt : Option[WidgetInput] = None
  private var lasftFocusedDirectoryTable:Option[DirectoryTable] = None

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

    leftPanel.focus.onAccept  >> { lasftFocusedDirectoryTable = Some(leftPanel) }
    rightPanel.focus.onAccept >> { lasftFocusedDirectoryTable = Some(rightPanel) }

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
          case MainMenu.DirTableName.Left  => MainMenu.tableColumns(leftPanel,  FilesTable.columns)
          case MainMenu.DirTableName.Right => MainMenu.tableColumns(rightPanel, FilesTable.columns)
      }
      MainMenu.defaultMenu.buildMenuBar
    }

  def executorOf(action:Action)(using ses:Session, conf:UiConf): ()=>Unit =
    import conf.given
    action match
      case Action.Exit => ()=>{ ses.stop = true }
      case Action.ActivateMainMenu => ()=>{ mbarOpt.foreach { mbar => mbar.focus.request } }
      case Action.MkDir => ()=>{
        lasftFocusedDirectoryTable.foreach { dirTable =>
          dirTable.directory.get.foreach { dir =>
            MkDirDialog.open( dir ).ok.listen { _ =>
              dirTable.refresh
            }
          }
        }
      }

  enum Action(val name:String):
    case Exit extends Action("Exit")
    case ActivateMainMenu extends Action("Show menu")
    case MkDir extends Action("MkDir")
