package xyz.cofe.jtfm.ui.table

import javax.swing.JTable

trait SwingTableFocus[A](using model:ListTableModel[A]) extends JTable:
  def focused: Option[A] = 
    this.getSelectionModel().getLeadSelectionIndex() match
      case ridx : Int if ridx<0 => None
      case ridx : Int => model.get(ridx)
      case _ => None

  def focused_=(row: Option[A]): Option[A] = 
    val prev = focused
    row match
      case None => this.getSelectionModel().setLeadSelectionIndex(-1)
      case Some(row0) => model.indexOf(row0).getOrElse(-1) match
        case ridx:Int if ridx<0 => this.getSelectionModel().setLeadSelectionIndex(-1)
        case ridx:Int => 
          this.getSelectionModel().setLeadSelectionIndex(ridx)
    prev

  def focusedRow:Option[Int] = 
    val row = this.getSelectionModel().getLeadSelectionIndex()
    if row<0 then None else Some(row)

  def focusedRow_=(row:Option[Int]):Option[Int] =
    val last = focusedRow
    row match
      case None => this.getSelectionModel().setLeadSelectionIndex(-1)
      case Some(row0) => row0 match
        case ridx:Int if ridx<0 => this.getSelectionModel().setLeadSelectionIndex(-1)
        case ridx:Int => 
          this.getSelectionModel().setLeadSelectionIndex(ridx)
    last
