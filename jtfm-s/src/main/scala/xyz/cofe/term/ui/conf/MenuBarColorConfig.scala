package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

trait MenuBarColorConfig:
  def backgroundColor: Color

object MenuBarColorConfig:
  given defaultConfig: MenuBarColorConfig = Conf()

  case class Conf(
    backgroundColor: Color = Color.CyanBright
  ) extends MenuBarColorConfig
