package xyz.cofe.jtfm.ui.table

import javax.swing.event.TableModelListener
import javax.swing.table.TableModel
import javax.swing.event.TableModelEvent

class SwingTableModel[A]( val table:Table[A] ) extends TableModel {
  var rows:List[A] = table.rows.toList
  var listeners = List[TableModelListener]()

  override def getColumnClass(columnIndex: Int): Class[?] = table.columns(columnIndex).dataClass

  override def addTableModelListener(l: TableModelListener): Unit = 
    listeners = l :: listeners

  override def getValueAt(rowIndex: Int, columnIndex: Int): Object = table.columns(columnIndex).asInstanceOf[Column[A,Object]].read(rows(rowIndex))

  override def isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false

  override def removeTableModelListener(l: TableModelListener): Unit = 
    listeners = listeners.filterNot( ls => ls==l )

  override def getColumnCount(): Int = table.columns.size

  override def getColumnName(columnIndex: Int): String = table.columns(columnIndex).name

  override def getRowCount(): Int = rows.size

  override def setValueAt(aValue: Object, rowIndex: Int, columnIndex: Int): Unit = ()

  def fireDataChanged():Unit =
    listeners.foreach { ls =>
      ls.tableChanged(new TableModelEvent(this))
    }

  def refresh():Unit = {
    rows = table.rows.toList
    fireDataChanged()
  }
}
