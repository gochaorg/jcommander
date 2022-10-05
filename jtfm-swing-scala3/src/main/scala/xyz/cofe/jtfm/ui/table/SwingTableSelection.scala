package xyz.cofe.jtfm.ui.table

import javax.swing.JTable
import xyz.cofe.jtfm.ObserverList
import javax.swing.event.ListSelectionListener
import javax.swing.event.ListSelectionEvent

trait SwingTableSelection[A](using model:ListTableModel[A]) extends JTable:
  def selected: List[A] = 
    this.getSelectedRows().toList.flatMap { ridx => model.get(ridx) }

  def selected_=(rows: List[A]): List[A] = 
    val prev = selected
    if( rows.isEmpty ) {
      this.getSelectionModel().clearSelection()
    } else {
      this.getSelectionModel().clearSelection()
      rows.foreach { row =>
        model.indexOf(row).foreach { ridx =>
          this.getSelectionModel().addSelectionInterval(ridx,ridx)
        }
      }
    }
    prev
