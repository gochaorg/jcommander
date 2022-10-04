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
import java.awt.BorderLayout
import java.awt.Desktop
import xyz.cofe.jtfm.ui.table.SwingDynTableModel
import javax.swing.JTable
import javax.swing.JScrollPane
import xyz.cofe.jtfm.ui.table.DynTable
import xyz.cofe.jtfm.ui.table.ListTableModel
import javax.swing.JSplitPane

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

      val splitPanel = new JSplitPane()
      splitPanel.setLeftComponent(new JScrollPane(swingTable))
      splitPanel.setRightComponent(new JScrollPane(selSwingTable))
      
      frame.getContentPane().add(splitPanel)
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
      .horizGlue()
      .menu("Table"){ mb => 
        mb.action("add") { data.insert(Sample("row"+data.size,data.size*2)) }
        mb.action("next") { swingTable.focusedRow = swingTable.focusedRow.map { _ + 1} }
        mb.action("prev") { swingTable.focusedRow = swingTable.focusedRow.map { _ - 1} }
      }
      .bar
  }

  case class Sample(name:String, cnt:Int)

  lazy val data = ObserverList[Sample]()
  lazy val columns = ObserverList(List(
    SwingDynTableModel.column[Sample,String]("name", sample=>sample.name, classOf[String]),
    SwingDynTableModel.column[Sample,Int]("cnt", sample=>sample.cnt, classOf[Int]),
    ))
  lazy val tableModel = new SwingDynTableModel[Sample](data, columns)
  lazy val swingTable = new DynTable(tableModel)

  lazy val selTableModel = new SwingDynTableModel[Sample](swingTable.selection, columns)
  lazy val selSwingTable = new DynTable(selTableModel)
}
