package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Position
import xyz.cofe.lazyp.Prop

trait LocationProp:
  def location:Prop[Position]

trait LocationRWProp extends Widget with LocationProp:
  val location:ReadWriteProp[Position] = ReadWriteProp(Position(0,0))
  location.onChange { repaint }
  def location_=(newPos:Position):Unit = location.set(newPos)

