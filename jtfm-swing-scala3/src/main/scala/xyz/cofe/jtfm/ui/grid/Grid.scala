package xyz.cofe.jtfm.ui.grid

import javax.swing.JComponent
import xyz.cofe.jtfm.ObserverList

class Grid extends JComponent:
  val columnSize = ObserverList[Int]()
  val rowSize = ObserverList[Int]()
