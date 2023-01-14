package xyz.cofe.term.ui.table

import xyz.cofe.term.cs.ObserverListImpl
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.ui.Widget

class Columns[A] extends ObserverListImpl[Column[A,_]]:  
  private var columnsListeners = Map[Column[A,_], List[ReleaseListener]]()
  onInsert { col =>     
    val ls = List(
      col.title.onChange(fireChanged()),
      col.width.onChange(fireChanged()),
      col.preferredWidth.onChange(fireChanged()),
      col.horizontalAlign.onChange(fireChanged()),
      col.rightDelimiter.onChange(fireChanged()),
      col.leftDelimiter.onChange(fireChanged()),
    )
    columnsListeners = columnsListeners + ( col -> (ls ++ columnsListeners.getOrElse(col, List.empty)) )
  }
  onDelete { col =>
    columnsListeners.getOrElse(col,List.empty).foreach(_.release())
    columnsListeners = columnsListeners.filterKeys( c => c!=col ).toMap
  }

trait ColumnsProp[A] extends Widget:
  val columns = Columns[A]()
  columns.onChange(repaint)
