package xyz.cofe.term.ui.table

import TableInput._
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.term.common.KeyName
import xyz.cofe.json4s3.stream.ast.FormattingJson
import xyz.cofe.json4s3.derv._

class TableInputMapTest extends munit.FunSuite:
  test("sample") {
    val lst = List(
      KeyStrokeBinding(
        keyStroke= KeyStroke.KeyEvent(KeyName.Down,altDown = false,ctrlDown = false,shiftDown = false),
        actions= List(
          Action.FocusMove(
            direction = FocusMoveDirection.Down, 
            selection = MoveSelection.Target),
        )
      )
    )

    implicit val fmt = FormattingJson.pretty(true)
    println(lst.json)
  }

