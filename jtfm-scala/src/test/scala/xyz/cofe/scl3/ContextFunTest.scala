package xyz.cofe.scl3

import org.junit.Test

class ContextFunTest {
  class Table {
    var rows = List[Row]()
    def add(row:Row):Unit = rows = rows :+ row
  }
  class Row {
    var cells = List[Cell]()
    def add(cell:Cell):Unit = cells = cells :+ cell
  }
  class Cell( val str:String ) {
  }

  def table( init: Table ?=> Unit ):Table = {
    given t:Table = Table()
    init
    t
  }
  def row( init: Row ?=> Unit )(using t:Table ):Row = {
    given r:Row = Row()
    init
    t.add(r)
    r
  }
  def cell( init:String )(using r:Row ):Cell = {
    given c:Cell = Cell(init)
    r.add( c )
    c
  }

  @Test
  def test01():Unit={
    val tbl = table {
      row {
        cell("a")
        cell("b")
      }
      row {
        cell("c")
        cell("d")
      }
    }
    tbl.rows.foreach { row => 
      println("row:")
      row.cells.foreach { cell =>
        println(s"  cell ${cell.str}")
      }
    }
  }
}
