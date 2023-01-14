package xyz.cofe.term.ui.table

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.cs.ObserverList

trait FocusedItem[A]( rows:ObserverList[A] ):
    val focusedIndex = Prop.rw(None:Option[Int])
    val focusedItem = Prop.eval(focusedIndex) { idxOpt =>
      idxOpt.flatMap { idx =>
        rows.getAt( idx )
      }
    }
