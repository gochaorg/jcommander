package xyz.cofe.jtfm.ui.table

import javax.swing.JTable
import javax.swing.JScrollPane

class SwingTableController[A](jtable:JTable, jscroll:JScrollPane, model:SwingTableModel[A]) extends TableController[A] {
  override def data: Table[A] = model.table

  override def focused_=(row: Option[A]): Option[A] = 
    val prev = focused
    row match
      case None => jtable.getSelectionModel().setLeadSelectionIndex(-1)
      case Some(row0) => model.rows.indexOf(row) match
        case ridx:Int if ridx<0 => jtable.getSelectionModel().setLeadSelectionIndex(-1)
        case ridx:Int => jtable.getSelectionModel().setLeadSelectionIndex(ridx)
    prev

  override def focused: Option[A] = 
    jtable.getSelectionModel().getLeadSelectionIndex() match
      case ridx : Int if ridx<0 => None
      case ridx : Int => Some(model.rows(ridx))
      case _ => None

  override def selected: List[A] = 
    jtable.getSelectedRows().toList.map { ridx => model.rows(ridx) }

  override def selected_=(rows: List[A]): List[A] = 
    val prev = selected
    if( rows.isEmpty ) {
      jtable.getSelectionModel().clearSelection()
    } else {
      jtable.getSelectionModel().clearSelection()
      rows.foreach { row =>
        val ridx = model.rows.indexOf(row)
        jtable.getSelectionModel().addSelectionInterval(ridx,ridx)
      }
    }
    prev
}