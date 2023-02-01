package xyz.cofe.term.ui.table

import TableInput._
import conf.TableInputConf
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.term.common.KeyName
import xyz.cofe.json4s3.stream.ast.FormattingJson
import xyz.cofe.json4s3.derv._

class TableInputMapTest extends munit.FunSuite:
  test("sample") {
    val inputConf = TableInputConf.Conf(keyboard = List(
      KeyStrokeBinding(
        KeyStroke.KeyEvent(KeyName.Down, altDown=false, ctrlDown=false, shiftDown=false),
        List(Action.FocusMove(direction = FocusMoveDirection.Down, selection = MoveSelection.NoChange))
      ),
        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.Up,       altDown=false, ctrlDown=false, shiftDown=false),
          List(Action.FocusMove(direction = FocusMoveDirection.Up, selection = MoveSelection.NoChange)),
        ),

        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.PageDown, altDown=false, ctrlDown=false, shiftDown=false),
          List(Action.FocusMove(direction = FocusMoveDirection.PageDown, selection = MoveSelection.NoChange)),
        ),

        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.PageUp,   altDown=false, ctrlDown=false, shiftDown=false),
          List(Action.FocusMove(direction = FocusMoveDirection.PageUp, selection = MoveSelection.NoChange)),
        ),

        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.Home,     altDown=false, ctrlDown=false, shiftDown=false),
          List(Action.FocusMove(direction = FocusMoveDirection.Home, selection = MoveSelection.NoChange)),
        ),

        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.End,      altDown=false, ctrlDown=false, shiftDown=false),
          List(Action.FocusMove(direction = FocusMoveDirection.End, selection = MoveSelection.NoChange)),          
        ),

        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.Down,     altDown=false, ctrlDown=false, shiftDown=true) ,
          List(Action.FocusMove(direction = FocusMoveDirection.Down, selection = MoveSelection.Include)),
        ),

        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.Up,       altDown=false, ctrlDown=false, shiftDown=true) ,
          List(Action.FocusMove(direction = FocusMoveDirection.Up, selection = MoveSelection.Include)),
        ),

        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.PageDown, altDown=false, ctrlDown=false, shiftDown=true) ,
          List(Action.FocusMove(direction = FocusMoveDirection.PageDown, selection = MoveSelection.Include)),
        ),

        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.PageUp,   altDown=false, ctrlDown=false, shiftDown=true) ,
          List(Action.FocusMove(direction = FocusMoveDirection.PageUp, selection = MoveSelection.Include)),
        ),

        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.Home,     altDown=false, ctrlDown=false, shiftDown=true) ,
          List(Action.FocusMove(direction = FocusMoveDirection.Home, selection = MoveSelection.Include)),
        ),

        KeyStrokeBinding(
          KeyStroke.KeyEvent(KeyName.End,      altDown=false, ctrlDown=false, shiftDown=true) ,
          List(Action.FocusMove(direction = FocusMoveDirection.End, selection = MoveSelection.Include)),
        ),

        KeyStrokeBinding(
          KeyStroke.CharEvent(' ', altDown=false, ctrlDown=false ,shiftDown=false),
          List(Action.FocusSelection(FocusSelAction.Invert)),
        ),

        KeyStrokeBinding(
          KeyStroke.CharEvent('a', altDown=false, ctrlDown=true ,shiftDown=false),
          List(Action.Selection(SelectWhat.All)),
        ),

        KeyStrokeBinding(
          KeyStroke.CharEvent('d', altDown=false, ctrlDown=true ,shiftDown=false),
          List(Action.Selection(SelectWhat.Clear)),
        ),

        KeyStrokeBinding(
          KeyStroke.CharEvent('i', altDown=false, ctrlDown=true ,shiftDown=false),
          List(Action.Selection(SelectWhat.Invert)),
        ),
    ))

    implicit val fmt = FormattingJson.pretty(true)
    println(inputConf.json)
  }

