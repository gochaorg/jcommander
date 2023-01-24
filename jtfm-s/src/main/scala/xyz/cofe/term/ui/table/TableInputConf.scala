package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.term.ui.Table

import TableInput._
import xyz.cofe.term.common.KeyName

trait TableInputConf:
  def keyStrokeActionMap( table:TableInput[_] ):Map[KeyStroke,()=>Unit]

object TableInputConf:
  given defaultConfig: TableInputConf with
    override def keyStrokeActionMap(table: TableInput[?]): Map[KeyStroke, () => Unit] = 
      Map(
        KeyStroke.KeyEvent(KeyName.Down,     altDown=false, ctrlDown=false, shiftDown=false) -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.Down, selection = MoveSelection.NoChange)),

        KeyStroke.KeyEvent(KeyName.Up,       altDown=false, ctrlDown=false, shiftDown=false) -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.Up, selection = MoveSelection.NoChange)),

        KeyStroke.KeyEvent(KeyName.PageDown, altDown=false, ctrlDown=false, shiftDown=false) -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.PageDown, selection = MoveSelection.NoChange)),

        KeyStroke.KeyEvent(KeyName.PageUp,   altDown=false, ctrlDown=false, shiftDown=false) -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.PageUp, selection = MoveSelection.NoChange)),

        KeyStroke.KeyEvent(KeyName.Home,     altDown=false, ctrlDown=false, shiftDown=false) -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.Home, selection = MoveSelection.NoChange)),

        KeyStroke.KeyEvent(KeyName.End,      altDown=false, ctrlDown=false, shiftDown=false) -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.End, selection = MoveSelection.NoChange)),          

        KeyStroke.KeyEvent(KeyName.Down,     altDown=false, ctrlDown=false, shiftDown=true)  -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.Down, selection = MoveSelection.Include)),

        KeyStroke.KeyEvent(KeyName.Up,       altDown=false, ctrlDown=false, shiftDown=true)  -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.Up, selection = MoveSelection.Include)),

        KeyStroke.KeyEvent(KeyName.PageDown, altDown=false, ctrlDown=false, shiftDown=true)  -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.PageDown, selection = MoveSelection.Include)),

        KeyStroke.KeyEvent(KeyName.PageUp,   altDown=false, ctrlDown=false, shiftDown=true)  -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.PageUp, selection = MoveSelection.Include)),

        KeyStroke.KeyEvent(KeyName.Home,     altDown=false, ctrlDown=false, shiftDown=true)  -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.Home, selection = MoveSelection.Include)),

        KeyStroke.KeyEvent(KeyName.End,      altDown=false, ctrlDown=false, shiftDown=true)  -> 
          table.executorOf(Action.FocusMove(direction = FocusMoveDirection.End, selection = MoveSelection.Include)),

        // KeyStroke.KeyEvent(KeyName.Insert,   altDown=false, ctrlDown=false, shiftDown=false) -> 
        //   table.executorOf(Action.FocusMove(direction = FocusMoveDirection.Down, selection = MoveSelection.Invert)),

        KeyStroke.CharEvent(' ', altDown=false, ctrlDown=false ,shiftDown=false) -> 
          table.executorOf(Action.FocusSelection(FocusSelAction.Invert)),

        KeyStroke.CharEvent('a', altDown=false, ctrlDown=true ,shiftDown=false) -> 
          table.executorOf(Action.Selection(SelectWhat.All)),

        KeyStroke.CharEvent('d', altDown=false, ctrlDown=true ,shiftDown=false) -> 
          table.executorOf(Action.Selection(SelectWhat.Clear)),

        KeyStroke.CharEvent('i', altDown=false, ctrlDown=true ,shiftDown=false) -> 
          table.executorOf(Action.Selection(SelectWhat.Invert)),
      )

  case class Conf(
    keyboard: List[KeyStrokeBinding]
  ) extends TableInputConf:
    override def keyStrokeActionMap(table: TableInput[?]): Map[KeyStroke, () => Unit] = 
      keyboard.map { ksBind => 
        ( ksBind.keyStroke
        , table.executorOf(ksBind.actions)
        )
      }.toMap
