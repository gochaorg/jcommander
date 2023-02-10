package xyz.cofe.jtfm
package conf

import MainMenu._
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.files.AppHome
import _root_.xyz.cofe.term.ui.menuBuilder.ActionConf
import xyz.cofe.json4s3.derv.FromJson
import xyz.cofe.term.ui.Session
import xyz.cofe.term.ui.{ 
  MenuBar => MBar,
  MenuContainer => MContainer,
  MenuAction => MAction,
  Menu => MMenu
}
import xyz.cofe.term.ui.prop.WidgetChildren
import xyz.cofe.term.ui.conf.MenuColorConfig
import xyz.cofe.term.ui.Table
import java.nio.file.Path
import xyz.cofe.term.ui.table.Column

case class MainMenu(
  keyboard: List[KeyBinding]
):
  lazy val actionKeystrokeMap = 
    keyboard.map { kb =>
      (kb.action.name, kb.keyStroke)
    }.toMap

object MainMenu:
  case class KeyBinding( action:Main.Action, keyStroke:KeyStroke )

  def confFile(appHome:AppHome):ConfFile[MainMenu] =
    ConfFile.Fallback(
      ConfFile.File(appHome.directory.resolve("main-menu.jsonc")),
      ConfFile.Resource("/default-config/main-menu.jsonc")
    )

  def read(using appHome:AppHome):Either[ConfError,MainMenu] =
    confFile(appHome).read

  enum MenuItem:
    case Root( items:List[MenuItem] ) extends MenuItem with RootOps
    case Menu( name:String, items:List[MenuItem]=List() ) extends MenuItem with MenuOps
    case MainAction( action:Main.Action ) extends MenuItem with MainActionOps
    case DirTable( name:DirTableName ) extends MenuItem

  enum DirTableName:
    case Left,Right

  import MenuItem._
  val defaultMenu:Root = Root(List(
    Menu("Left", List(
      Menu("Columns", List(
        DirTable(DirTableName.Left)
      ))
    )),
    Menu("File",List(
      MainAction(Main.Action.MkDir),
      MainAction(Main.Action.Exit)
    )),
    Menu("View", List(
      MainAction(Main.Action.ActivateMainMenu)
    )),
    Menu("Right", List(
      Menu("Columns", List(
        DirTable(DirTableName.Right)
      ))
    ))
  ))

  trait RootOps:
    self: Root =>
      def buildMenuBar(using 
        conf:UiConf, 
        ses:Session,
        executorOf:Main.Action=>()=>Unit,
        keyboard:Map[String,KeyStroke],
        dirTableMenu: (DirTableName,WidgetChildren[MMenu]) => Unit,
      ):MBar =        
        import xyz.cofe.term.ui.menuBuilder._
        import conf.given
        menuBar {
          self.items.foreach {
            case _:Root => ()
            case m:Menu => m.buildMenu
            case m:MainAction => m.buildAction
            case m:DirTable => 
              dirTableMenu(m.name, summon[WidgetChildren[MMenu]])
          }
        }

  trait MenuOps:
    self: Menu =>
      def buildMenu( using 
        mConf:MenuColorConfig, 
        menuParent:WidgetChildren[MMenu],
        executorOf:Main.Action=>()=>Unit,
        keyboard:Map[String,KeyStroke],
        dirTableMenu: (DirTableName,WidgetChildren[MMenu]) => Unit
      ):MContainer =
        import xyz.cofe.term.ui.menuBuilder._
        menu(self.name) {          
          self.items.foreach {
            case _:Root => ()
            case m:Menu => m.buildMenu
            case m:MainAction => m.buildAction
            case m:DirTable => dirTableMenu(m.name, summon[WidgetChildren[MMenu]])
          }
        }

  trait MainActionOps:
    self: MainAction =>
      def buildAction(using 
        menuParent:WidgetChildren[MMenu],
        mConf:MenuColorConfig,
        executorOf:Main.Action=>()=>Unit,
        keyboard:Map[String,KeyStroke],
      ):MAction =
        import xyz.cofe.term.ui.menuBuilder.{action => maction}
        val menuAct = maction(self.action.name)
        val exec = executorOf(action)
        keyboard.get(action.name)
          .map(ks => menuAct.keyStroke(ks).apply(exec()))
          .getOrElse( menuAct.apply(exec()) )

  def tableColumns( table:Table[Path], available:List[Column[Path,?]] )
      ( using 
        menuParent:WidgetChildren[MMenu], 
        config: MenuColorConfig
      )
  :Unit = {
    import xyz.cofe.term.ui.menuBuilder._

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
