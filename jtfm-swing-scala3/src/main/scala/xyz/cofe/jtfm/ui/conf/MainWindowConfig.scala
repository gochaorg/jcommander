package xyz.cofe.jtfm
package ui.conf

import xyz.cofe.jtfm.ui.WindowLocation
import xyz.cofe.jtfm.ui.WindowState
import javax.swing.JFrame
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

case class MainWindowConfig(
 location: WindowLocation = WindowLocation(WindowState.Normal, 100,100, 600, 600)
)

object MainWindowConfig:
  object lens:
    val location = Lens[MainWindowConfig,WindowLocation](
      get= a => a.location,
      set= (a,b) => a.copy(location = b)
    )