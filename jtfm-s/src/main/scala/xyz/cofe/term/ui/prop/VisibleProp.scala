package xyz.cofe.term.ui
package prop

import xyz.cofe.lazyp.ReadWriteProp


class VisibleClient( widget:Widget ):
  val value:ReadWriteProp[Boolean] = ReadWriteProp(true)
  def inTree:Boolean = 
    widget.toTreePath.listToLeaf.forall { w => w.visible.value.get
    }

implicit def visibleClient2Bool( vc:VisibleClient ):Boolean = vc.value.get

