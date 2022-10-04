package xyz.cofe.jtfm.ui.table

import xyz.cofe.jtfm.ui.ObserverList
import javax.swing.table.TableModel
import javax.swing.event.TableModelListener
import javax.swing.event.TableModelEvent
import java.awt.Event
import xyz.cofe.jtfm.ui.ObserverListEvent

class SwingDynTableModel[A]( data:ObserverList[A], columns:ObserverList[_ <: SwingDynTableModel.Column[A,_]] ) extends TableModel:
  given listTableModel: ListTableModel[A] with
    def indexOf(item:A):Option[Int] = data.indexOf(item)
    def get(row:Int):Option[A] = data.get(row)


  private val dataListener = data.listen { ev => 
    println(ev)
    ev match
      case ins:ObserverListEvent.Insert[A] => 
        fireRowInserted(ins.idx)
      case upd:ObserverListEvent.Update[A] => 
        fireRowUpdated(upd.idx)
      case del:ObserverListEvent.Delete[A] => 
        fireRowDeleted(del.idx)
  }
  private val columnsListener = columns.listen { ev =>
    fireDataChanged()
  }

  var listeners = List[TableModelListener]()

  override def getColumnCount(): Int = columns.size
  override def getColumnClass(columnIndex: Int): Class[?] = columns(columnIndex).dataClass
  override def getColumnName(columnIndex: Int): String = columns(columnIndex).name

  override def getRowCount(): Int = data.size
  override def getValueAt(rowIndex: Int, columnIndex: Int): Object = columns(columnIndex).asInstanceOf[SwingDynTableModel.Column[A,Object]].read(data(rowIndex))
  override def setValueAt(aValue: Object, rowIndex: Int, columnIndex: Int): Unit = ()

  override def isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false  

  override def addTableModelListener(l: TableModelListener): Unit = 
    listeners = l :: listeners
  override def removeTableModelListener(l: TableModelListener): Unit = 
    listeners = listeners.filterNot( ls => ls==l )

  def fireDataChanged():Unit =
    listeners.foreach { ls => ls.tableChanged(new TableModelEvent(this)) }

  def fireRowUpdated(row:Int):Unit =
    listeners.foreach { ls => ls.tableChanged(new TableModelEvent(this, row)) }

  def fireRowInserted(row:Int):Unit =
    listeners.foreach { ls => ls.tableChanged(new TableModelEvent(this, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)) }

  def fireRowDeleted(row:Int):Unit =
    listeners.foreach { ls => ls.tableChanged(new TableModelEvent(this, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE)) }

object SwingDynTableModel:
  trait Column[A,V]:
    def name:String
    def dataClass:Class[V]
    def read(row:A):V
  case class SimpleColumn[A,V]( columnName:String, reader:A=>V, columnDataClass:Class[V] ) extends Column[A,V]:
      def name:String = columnName
      def dataClass:Class[V] = columnDataClass
      def read(row:A):V = reader(row)

  def column[A,V]( columnName:String, reader:A=>V, columnDataClass:Class[V] ) = SimpleColumn[A,V](columnName,reader,columnDataClass)
