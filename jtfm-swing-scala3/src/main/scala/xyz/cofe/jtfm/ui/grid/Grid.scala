package xyz.cofe.jtfm.ui.grid

import javax.swing.JComponent
import xyz.cofe.jtfm.ObserverList
import xyz.cofe.jtfm.ObserverMap

case class Cell(width:Int=1, height:Int=1)
case class Location(left:Int,top:Int)

class Grid extends JComponent:

  private val columnSize = ObserverList[Int]()
  private val rowSize = ObserverList[Int]()
  private val cellMap = ObserverMap[Int, ObserverMap[Int,Cell]]()
  private var defaultWidth:Int = 75
  private var defaultHeight:Int = 30
  def cells = 
    (for { 
      yMap   <- cellMap.iterator
      y      =  yMap._1
      xMap   =  yMap._2
      xCell  <- xMap.iterator
      x      =  xCell._1
      cell   =  xCell._2
    } yield (x,y,cell)).map { case (x,y,c) => (Location(x,y), c) }
