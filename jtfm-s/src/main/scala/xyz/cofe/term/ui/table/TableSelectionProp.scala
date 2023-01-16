package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.Widget
import xyz.cofe.term.cs.ObserverListImpl
import xyz.cofe.term.cs.ObserverList
import xyz.cofe.term.cs.ObserverSet
import xyz.cofe.lazyp.Prop

trait TableSelectionProp[A] extends TableRowsProp[A]:
  val selection = new TableSelection(rows)
  selection.indexes.onChange(repaint)
  selection.focusedIndex.onChange(repaint)

class TableSelection[A]( rows0:ObserverList[A] ) extends FocusedItem[A]( rows0 ):
  val indexes : ObserverSet[Int] = ObserverSet.sorted[Int]
  val rows:Prop[List[A]] = Prop.eval( rows0, indexes ){ case (rows, indexes) => 
    indexes.map { idx =>
      rows.getAt(idx)
    }.foldLeft( List.empty[A] ){ case(lst,itmOpt) => 
      itmOpt.map { itm => itm :: lst }.getOrElse( lst )
    }
  }