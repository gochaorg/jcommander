package xyz.cofe.term.ui.table

trait CellText[V]:
  def cellTextOf( value:V ):String
