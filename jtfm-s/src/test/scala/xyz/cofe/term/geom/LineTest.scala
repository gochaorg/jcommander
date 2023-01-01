package xyz.cofe.term.geom

import xyz.cofe.term.common.Position

class LineTest extends munit.FunSuite:
  test("hv") {
    val l = Line(Position(0,0), Position(10,0), Symbols.Style.Single)
    assert(!l.dot)
    assert(!l.diagonal)
    
    val hvl = l.toHVLine()
    assert(hvl.isDefined)
  }

  test("diff") {
    val x = Position(0,0) diff Position(10,0)
    println(x)
  }