package xyz.cofe.jtfm

import xyz.cofe.files.AppHome
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
import xyz.cofe.jtfm.conf.UiConf
import xyz.cofe.files.FilesLogger
import org.slf4j.LoggerFactory
import xyz.cofe.term.ui.Table
import xyz.cofe.term.ui.table.Column
import xyz.cofe.jtfm.ui.table.FilesTable
import xyz.cofe.term.ui.Menu
import xyz.cofe.term.ui.conf.MenuColorConfig
import xyz.cofe.term.ui.MenuAction
import xyz.cofe.jtfm.conf.MainMenu

object Main:
  implicit object appHome extends AppHome("jtfm")

  def main(args:Array[String]):Unit =
    LogPrepare.prepare
    HelloMessage.writeLog
    ConsoleBuilder.useConsole(startSession)

  private var mbarOpt : Option[WidgetInput] = None

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

  enum Action(val name:String):
    case Exit extends Action("Exit")
    case ActivateMainMenu extends Action("Show menu")

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
