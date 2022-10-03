package xyz.cofe.jtfm.ui

import javax.swing.SwingUtilities
import javax.swing.JFrame
import javax.swing.WindowConstants
import java.awt.event.WindowStateListener
import java.awt.event.WindowListener
import java.awt.event.WindowAdapter
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import xyz.cofe.jtfm.AppConfig
import xyz.cofe.jtfm.ui.conf._
import javax.swing.JMenuBar
import javax.swing.JMenu
import java.awt.GraphicsEnvironment
import xyz.cofe.jtfm.ui.table.Column
import xyz.cofe.jtfm.ui.table.BasicColumn
import xyz.cofe.jtfm.ui.table.BasicTable
import xyz.cofe.jtfm.ui.table.Table
import xyz.cofe.jtfm.ui.table.TableController
import java.awt.BorderLayout
import java.awt.Desktop

object Main {
  def main(args:Array[String]):Unit = {
    AppConfig.activeConfig.swing.apply()

    SwingUtilities.invokeLater(() => {
      val frame = new JFrame("JTFM")
      frame.setJMenuBar(mainMenu)

      WindowLocation.apply(frame, AppConfig.activeConfig.mainWindow.location)

      val mainWindowLocLens = AppConfig.lens.mainWindow + MainWindowConfig.lens.location
      WindowLocation.listen(frame) { loc => 
        AppConfig.activeConfig = mainWindowLocLens(AppConfig.activeConfig,loc)
      }

      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      frame.setVisible(true)

      frame.getContentPane().setLayout(new BorderLayout())
      frame.getContentPane().add(tableControllers._2)
    })
  }

  lazy val mainMenu:JMenuBar = {
    MenuBuilder(new JMenuBar)
      .menu("File"){ mb => 
        mb.action("exit"){ System.exit(0) }
      }
      .menu("Configuration") { mb =>
        mb.action("Show config file") {
          Desktop.getDesktop() match
            case null => ()
            case desk => desk.open( AppConfig.configFile.toFile() )
        }
      }
      //.horizGlue()
      //.menu("Right"){ mb => }
      .bar
  }

  case class Sample(name:String, cnt:Int)
  val nameColumn = BasicColumn[Sample,String]("name", row=>row.name, classOf[String])
  val cntColumn = BasicColumn[Sample,Int]("count", row=>row.cnt, classOf[Int])
  val table = BasicTable(
    List(nameColumn, cntColumn),
    List(Sample("hello", 1), Sample("world", 2))
  )
  lazy val tableControllers = TableController.swing(table)
}
