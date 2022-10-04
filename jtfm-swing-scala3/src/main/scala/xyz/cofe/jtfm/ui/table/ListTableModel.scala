package xyz.cofe.jtfm.ui.table

trait ListTableModel[A]:
  def indexOf(item:A):Option[Int]
  def get(row:Int):Option[A]

