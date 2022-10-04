package xyz.cofe.jtfm.ui.table

import javax.swing.JTable
import javax.swing.table.TableModel
import javax.swing.event.ListSelectionListener
import javax.swing.event.ListSelectionEvent

class DynTable[A,M <: SwingDynTableModel[A]]( 
  model:M 
)(implicit 
  listTableModel: ListTableModel[A] = model.listTableModel
) extends JTable 
  with SwingTableFocus[A]
  with SwingTableSelection[A]
  with SwingTableDynSelection[A]:
  setModel(model)
