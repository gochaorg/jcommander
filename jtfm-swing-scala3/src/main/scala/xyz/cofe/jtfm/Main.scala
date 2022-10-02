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
    })
  }

  lazy val mainMenu:JMenuBar = {
    MenuBuilder(new JMenuBar)
      .menu("Left"){ mb => }
      .horizGlue()
      .menu("Right"){ mb => }
      .bar
  }
}
