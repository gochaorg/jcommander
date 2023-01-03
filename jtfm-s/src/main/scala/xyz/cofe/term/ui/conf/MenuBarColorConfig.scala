package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

trait MenuBarColorConfig:
  def backgroundColor: Color = Color.CyanBright

object MenuBarColorConfig:
  given defaultConfig: MenuBarColorConfig = new MenuBarColorConfig {}
