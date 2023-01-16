package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.Widget
import xyz.cofe.lazyp.Prop

trait TableScrollProp extends Widget:
  object scroll:
    val value = Prop.rw(0)
    value.onChange(repaint)
