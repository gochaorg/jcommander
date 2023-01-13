package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Position

trait LocationRWProp extends Widget:
  val location:ReadWriteProp[Position] = ReadWriteProp(Position(0,0))
  location.onChange { repaint }
  def location_=(newPos:Position):Unit = location.set(newPos)

