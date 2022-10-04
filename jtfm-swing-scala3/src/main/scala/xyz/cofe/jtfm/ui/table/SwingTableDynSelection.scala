package xyz.cofe.jtfm.ui.table

import xyz.cofe.jtfm.ui.ObserverList
import javax.swing.JTable
import javax.swing.event.ListSelectionListener
import javax.swing.event.ListSelectionEvent

trait SwingTableDynSelection[A](using model:ListTableModel[A]) extends JTable:  
  val selection:ObserverList[A] = ObserverList[A]()
  private var selectionRows:Set[Int] = Set()

  private var syncRunning = false
  private def sync():Unit =
    try
      syncRunning = true
      
      var nowSelectedRows = Set[Int]()
      getSelectedRows().foreach { ri => nowSelectedRows = nowSelectedRows + ri }

      val incRows = nowSelectedRows.filter { ri => !selectionRows.contains(ri) }
      val excRows = selectionRows.filter { ri => !nowSelectedRows.contains(ri) }

      incRows.foreach { ri =>
        selectionRows = selectionRows + ri
        model.get(ri) match
          case Some(item) => 
            selection.insert(item)
          case None =>
      }

      excRows.foreach { ri =>
        selectionRows = selectionRows - ri
        model.get(ri) match
          case Some(item) =>
            selection.delete(item)
          case None =>
      }
    finally
      syncRunning = false

  getSelectionModel().addListSelectionListener(new ListSelectionListener(){
    override def valueChanged(e: ListSelectionEvent): Unit = {      
      sync()
    }
  })

