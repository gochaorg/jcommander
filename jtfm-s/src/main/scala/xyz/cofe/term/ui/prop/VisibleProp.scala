package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReadWriteProp

trait VisibleProp extends Widget:
  val visible = VisibleClient(this)
  visible.value.onChange { repaint }

  def visible_=( value:Boolean ):Unit = visible.value.set(value)

class VisibleClient( widget:Widget ):
  val value:ReadWriteProp[Boolean] = ReadWriteProp(true)
  def inTree:Boolean = 
    widget.toTreePath.listToLeaf.forall {
      case wv:VisibleProp => wv.visible.value.get
      case _ => false
    }

implicit def visibleClient2Bool( vc:VisibleClient ):Boolean = vc.value.get

