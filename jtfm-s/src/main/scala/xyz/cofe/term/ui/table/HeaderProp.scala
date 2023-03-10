package xyz.cofe.term.ui
package table

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.ui.Widget
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.ui.Listener

trait HeaderProp extends Widget:
  val header = HeaderProp.Header(repaint = repaint)

object HeaderProp:
  class Header(repaint: =>Unit) extends Prop[Header]:
    val visible = Prop.rw(true)
    visible.onChange(repaint)
    visible.onChange(listeners.emit())

    val delimiter = Prop.rw(Delimeter.DoubleLine)
    delimiter.onChange(repaint)
    delimiter.onChange(listeners.emit())

    override def get: Header = this
    private val listeners : Listener[Unit] = Listener.unit

    override def onChange(ls: => Unit): ReleaseListener = 
      listeners.listen( _ => ls )
