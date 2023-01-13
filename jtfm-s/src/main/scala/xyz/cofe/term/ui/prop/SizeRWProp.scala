package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Size

trait SizeRWProp extends Widget:
  val size:ReadWriteProp[Size] = ReadWriteProp(Size(1,1))
  size.onChange { repaint }
  def size_=( newSize:Size ):Unit = size.set(newSize)

