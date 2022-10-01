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

object Main {
  def main(args:Array[String]):Unit =
    SwingUtilities.invokeLater(() => {
      val frame = new JFrame("JTFM")
      frame(AppConfig.activeConfig.mainWindow.location)
      AppConfig.activeConfig.mainWindow.listen(frame)

      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      frame.setVisible(true)
    })
}
