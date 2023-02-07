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

object Main:
  implicit object appHome extends AppHome("jtfm")
  implicit val metricConf:MetricConf = MetricConf.read.getOrElse(MetricConf.defaultConf)
  private implicit lazy val logger : Logger = LoggerFactory.getLogger("xyz.cofe.jtfm.Main")

  def main(args:Array[String]):Unit =
    LogPrepare.prepare
    HelloMessage.writeLog
    metricConf.run {
      ConsoleBuilder.useConsole(startSession)
    }

  private var mbarOpt : Option[WidgetInput] = None
  private var lasftFocusedDirectoryTable:Option[DirectoryTable] = None
  def startSession( console: Console ):Unit =
    val conf : UiConf = new UiConf
    import conf.given

    Session.start(console) { implicit ses =>
      import xyz.cofe.term.ui.menuBuilder._

      val leftPanel  = new DirectoryTable
      val rightPanel = new DirectoryTable

      leftPanel.directory.set(Some(Path.of(".")))
      rightPanel.directory.set(Some(Path.of(".")))

      leftPanel.keyStrokeMap.bind(KeyStroke.KeyEvent(KeyName.Tab,false,false,false), ()=>{ rightPanel.focus.request })
      rightPanel.keyStrokeMap.bind(KeyStroke.KeyEvent(KeyName.Tab,false,false,false), ()=>{ leftPanel.focus.request })

      leftPanel.focus.onAccept  >> { lasftFocusedDirectoryTable = Some(leftPanel) }
      rightPanel.focus.onAccept >> { lasftFocusedDirectoryTable = Some(rightPanel) }

      val vsplitPanel = VSplitPane()
      ses.rootWidget.children.append(vsplitPanel)
      vsplitPanel.bind( ses.rootWidget ){ case (root) => (root.leftTop + (0,1), root.rightBottom).rect }
      
      vsplitPanel.leftWidget.set(Some(leftPanel))
      vsplitPanel.rightWidget.set(Some(rightPanel))

      ses.requestFocus(leftPanel)
      leftPanel.selection.focusedIndex.set(Some(1))

      menuBar {
        def tableColumns( table:Table[Path], available:List[Column[Path,?]] )
            ( using 
              menuParent:WidgetChildren[Menu], 
              config: MenuColorConfig
            )
        :Unit = {
          available.zipWithIndex.foreach { case (column,colIdx) =>
            def hasColumn:Boolean = table.columns.toList.map(_.id).contains(column.id)

            var updateText:Option[()=>Any] = None

            val actCol = action(column.id) {  
              if hasColumn 
              then
                table.columns.items.find(_.id == column.id).foreach { col => table.columns.delete(col) }
                updateText.foreach(_())
              else                
                table.columns.insert( colIdx, column )
                table.autoResizeColumnsDeferred()
                updateText.foreach(_())
            }

            updateText = Some { ()=>{
              actCol.text.set( (if hasColumn then "+" else "-") + " " + column.id )
            }}
            updateText.foreach(_())
          }
        }

        implicit val executorBuilder: Action=>()=>Unit = { act => 
          executorOf(act) 
        }

        menu("Left") {
          menu("Columns") {
            tableColumns(leftPanel, FilesTable.columns)
          }
        }
        menu("File") {
          Action.MkDir.menuAction
          Action.Exit.menuAction
        }
        
        mbarOpt = Some(menu("View") {
          Action.ActivateMainMenu.menuAction
        })

        menu("Right") {
          menu("Columns") {
            tableColumns(rightPanel, FilesTable.columns)
          }
        }
      }
    }

  def executorOf(action:Action)(using ses:Session): ()=>Unit =
    action match
      case Action.Exit => ()=>{ ses.stop = true }
      case Action.ActivateMainMenu => ()=>{ mbarOpt.foreach { mbar => mbar.focus.request } }
      case Action.MkDir => ()=>{
        lasftFocusedDirectoryTable.foreach { dirTable =>
          Dialog
            .title("mk dir")
            .size(36,15)
            .content { (panel,hdl) =>              
              val input = TextField()

              panel.children.append(input)
              input.bind(panel) { b =>                 
                Rect(0,1,b.width,1)
              }

              hdl.onOpen { 
                debug"input.focus.request"
                input.focus.request 
              }
            }
            .open()
        }
      }

  enum Action(val name:String):
    case Exit extends Action("Exit")
    case ActivateMainMenu extends Action("Show menu")
    case MkDir extends Action("MkDir")

    def menuAction
        ( using 
          menuParent:WidgetChildren[Menu], 
          config: MenuColorConfig,
          executor: Action=>()=>Unit,
          mainMenu: MainMenu
        ):MenuAction = 
      import xyz.cofe.term.ui.menuBuilder._

      val actConf = action(this.name)
      val exec = executor(this)

      mainMenu.actionKeystrokeMap.get(this.name)
        .map { ks => actConf.keyStroke(ks).apply(exec()) }
        .getOrElse( actConf.apply(exec()) )
