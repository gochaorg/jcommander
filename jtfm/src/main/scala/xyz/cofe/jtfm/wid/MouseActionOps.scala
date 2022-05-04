package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.input.MouseAction

implicit class MouseActionOps(ma:MouseAction):
  def button:Option[MouseButton] = MouseButton(ma)

