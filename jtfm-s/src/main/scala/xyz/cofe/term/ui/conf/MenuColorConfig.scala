package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

trait MenuColorConfig extends FocusColorConfig:
  def keyStrokeFgColor : Color = Color.BlueBright

object MenuColorConfig:
  given defaultConfig: MenuColorConfig = new MenuColorConfig {}