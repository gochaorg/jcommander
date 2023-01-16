package xyz.cofe.term.ui
package prop

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Size
import xyz.cofe.lazyp.Prop

trait SizeProp:
  def size:Prop[Size]

trait SizeRWProp extends Widget with SizeProp:
  val size:ReadWriteProp[Size] = ReadWriteProp(Size(1,1))
  size.onChange { repaint }
  def size_=( newSize:Size ):Unit = size.set(newSize)

