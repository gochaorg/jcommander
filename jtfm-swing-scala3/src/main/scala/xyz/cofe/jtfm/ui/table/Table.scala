package xyz.cofe.jtfm.ui.table

import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.table.TableModel
import javax.swing.event.TableModelListener
import javax.swing.JScrollPane

trait CellRender[C,V]:
  def prepare(component:C, value:V):Unit

trait Column[A,V]:
  def name:String
  def dataClass:Class[V]
  def read(row:A):V

case class BasicColumn[A,V]( name:String, reading:A=>V, dataClass:Class[V] ) extends Column[A,V]:
  override def read(row: A): V = reading(row)

trait Table[A]:
  def columns:List[Column[A,_]]
  def rows:Iterator[A]

case class BasicTable[A]( columns:List[Column[A,_]], data:List[A] ) extends Table[A]:
  override def rows: Iterator[A] = data.iterator
  
trait TableController[A]:
  def data:Table[A]

  def selected:List[A]
  def selected_=(rows:List[A]):List[A]

  def focused:Option[A]
  def focused_=(row:Option[A]):Option[A]  

object TableController:
  def swing[A](table:Table[A]):(TableController[A],JComponent) = 
    val jtable = new JTable()
    val jscroll = new JScrollPane(jtable)    
    val tableModel = new SwingTableModel[A](table)
    val controller = new SwingTableController(jtable, jscroll, tableModel)
    jtable.setModel(tableModel)
    (controller, jscroll)

