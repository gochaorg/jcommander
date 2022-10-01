package xyz.cofe.jtfm.ui.conf

import xyz.cofe.jtfm.ui.WindowLocation
import xyz.cofe.jtfm.ui.WindowState
import javax.swing.JFrame
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

case class MainWindowConfig(
  var location: WindowLocation = WindowLocation(WindowState.Normal, 100,100, 600, 600)
):
  def listen(frame:JFrame):Unit =
    frame.addWindowStateListener { _ => 
      location = WindowLocation(frame)
    }
    frame.addComponentListener(new ComponentAdapter(){
      override def componentResized(e: ComponentEvent): Unit = 
        location = WindowLocation(frame)
      override def componentMoved(e: ComponentEvent): Unit = 
        location = WindowLocation(frame)
    })

