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
import xyz.cofe.files.log.PathPattern
import java.nio.file.Path
import xyz.cofe.files.log.AppendableFile.apply
import xyz.cofe.files.log.AppendableFile
import xyz.cofe.term.ui.ses.SesInputLog
import xyz.cofe.files.log.PathPattern.Evaluate
import scala.collection.immutable.LazyList.cons
import xyz.cofe.term.cs._
import xyz.cofe.jtfm.conf.ColorsConf
import xyz.cofe.term.ui.conf.MenuBarColorConfig
import xyz.cofe.term.ui.conf.MenuColorConfig
import xyz.cofe.jtfm.log.JsonLogOutput
import xyz.cofe.json4s3.derv.ToJson
import xyz.cofe.term.ui.table.Column
import xyz.cofe.term.ui.table.HorizontalAlign
import xyz.cofe.term.ui.table.conf.TableInputConf
import xyz.cofe.term.ui.table.conf.TableColorsConf
import xyz.cofe.jtfm.conf.TableConf
import xyz.cofe.jtfm.ui.table.FilesTable
import xyz.cofe.files.readDir
import xyz.cofe.jtfm.conf.ConfError

object TableMain:
  implicit object appHome extends AppHome("jtfm")
  implicit val pathPatternEval : Evaluate = Evaluate.defaultEvaluate

  lazy val logPathCommon = "{appHome}/log/{yyyy}/{MM}/{dd}/{HH}-{mi}-pid{pid}-"
  lazy val sesInputLogPath = PathPattern.parse(Path.of(s"${logPathCommon}sesInput.json"))
  lazy val sesInputOut = AppendableFile(sesInputLogPath,Some(1024*1024*16))
  given sesInputLog : SesInputLog = SesInputLog.writeTo(new JsonLogOutput[SesInputLog.SesInputEvent](sesInputOut))

  lazy val tableInputConfFile = TableConf.confFile(appHome)

  def main(args:Array[String]):Unit =
    //System.setProperty("xyz.cofe.term.default","telnet")
    //System.setProperty("xyz.cofe.term.telnet.port","12346")

    val colorsConf:Either[ConfError,ColorsConf] = ColorsConf.read
    implicit val menuBarColors = colorsConf.map(_.menu.bar).getOrElse(new MenuBarColorConfig.Conf)
    implicit val menuColors = colorsConf.map(_.menu.container).getOrElse(new MenuColorConfig.Conf)

    val tableConf:Either[ConfError,TableInputConf] = TableConf.read
    implicit val tableInputConf = tableConf.map(x => x:TableInputConf).getOrElse(TableInputConf.defaultConfig)
    implicit val tableColorsConf = TableColorsConf.defaultColors

    val console = ConsoleBuilder.defaultConsole()
    Session.start(console) { ses =>
      // ses.rootWidget.backgroundColor.set(Color.Green)

      // val pnl1 = new FocPanel("pnl1")
      // pnl1.location.set(Position(1,3))
      // pnl1.size.set(Size(30,1))
      // ses.rootWidget.children.append(pnl1)

      // val but1 = Button("exit").action { ses.stop = true }
      // but1.location.set(Position(35,2))
      // ses.rootWidget.children.append(but1)

      // val but2 = Button("foc on pnl1").action { pnl1.focus.request }
      // but2.location.set(Position(35,4))
      // ses.rootWidget.children.append(but2)

      // val but3 = Button("clear buff").action {
      //   (0 until ses.screenBuffer.height).flatMap { y => 
      //     (0 until ses.screenBuffer.width).map { x => (x,y) }
      //   }.foreach { case (x,y) => 
      //     ses.screenBuffer.set(x,y,ScreenChar(' ',Color.White,Color.Black))
      //   }
      // }
      // but3.location.set(Position(35,6))
      // ses.rootWidget.children.append(but3)

      // val but4 = Button("clear console").action {
      //   console.setBackground(Color.Black)
      //   console.setForeground(Color.White)
      //   (0 until ses.screenBuffer.height).flatMap { y => 
      //     (0 until ses.screenBuffer.width).map { x => (x,y) }
      //   }.foreach { case (x,y) => 
      //     console.setCursorPosition(x,y)
      //     console.write(" ")
      //   }
      // }
      // but4.location.set(Position(35,8))
      // ses.rootWidget.children.append(but4)

      // val but5 = Button("dump").action {
      //   ses.rootWidget.walk.path.foreach { path =>
      //     print("  "*path.rpath.size)
      //     val v = path.node match
      //       case vp:VisibleProp => s"${vp.visible.value.get}"
      //       case _ => "?"
          
      //     println(s"${path.node} v=${v} ${path.node.location.get} ${path.node.size.get}")
      //   }
      // }
      // but5.location.set(Position(35,10))
      // ses.rootWidget.children.append(but5)

      // val textField = new TextField()
      // textField.location = Position(1,5)
      // textField.size = Size(25,1)
      // textField.text = "sample"
      // ses.rootWidget.children.append(textField)

      ///////////////////////////////////////////////////////////////////////////

      val table = Table[Path]
      table.size = Size(60,25)
      table.location = Position(1,1)

      table.columns.append(FilesTable.columns)

      Path.of(".").readDir.foreach { files => 
        table.rows.append(
          FilesTable.sort( 
            Path.of("..") :: files, 
            FilesTable.sort.defaultSort )
        )
      }

      ses.rootWidget.children.append(table)

      val menuBar = new MenuBar
      val menuFile = MenuContainer("File")
      
      val menuFileOpen = 
        MenuAction("Open")
          .action { println("open") }
          .keyStroke( KeyStroke.KeyEvent(KeyName.F2,false,false,false) )

      val menuFileExit = MenuAction("Exit").action { println("exit"); ses.stop = true }        
      val menuView = MenuContainer("View")        
      val menuViewSome = MenuAction("Some")

      menuBar.children.append(menuFile)        
      menuBar.children.append(menuView)
      menuFile.children.append(menuFileOpen)
      menuFile.children.append(menuFileExit)

      menuView.children.append(
        MenuAction("Dialog 1").action {
          println("dialog 1")
          Dialog
            .title("title").size(Size(25,10))
            .onHide { println("closed") }
            .content { dlg => 
              val lbl = Label("label 12345")
              lbl.location = Position(0,0)
              dlg.children.append(lbl)
            }
            .show()
        }
      )

      menuView.children.append((
        MenuAction("columns size").action {
          table.columns.foreach( col => println(s"column ${col.id} ${col.width.get}") )
        }
      ))

      menuBar.install(ses.rootWidget)
    }

    console.close()