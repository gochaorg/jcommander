package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.input.MouseAction

enum MouseButton(val buttonNum:Int):
  case NoButton extends MouseButton(0)
  case Left extends MouseButton(1)
  case Right extends MouseButton(3)
  case Middle extends MouseButton(2)
  case WheelUp extends MouseButton(4)
  case WheelDown extends MouseButton(5)

object MouseButton:
  def apply(ma:MouseAction):Option[MouseButton]=
    MouseButton.values.find(_.buttonNum==ma.getButton())
