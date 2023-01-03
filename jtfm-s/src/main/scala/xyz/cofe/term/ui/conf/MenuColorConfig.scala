package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

trait MenuColorConfig extends FocusColorConfig:
  def keyStrokeFgColor : Color

object MenuColorConfig:
  given defaultConfig: MenuColorConfig = new Conf()

  case class Conf(
    focusContainerFgColor: Color = Color.WhiteBright,
    focusOwnerFgColor: Color = Color.YellowBright,
    focusContainerBgColor: Color = Color.BlackBright,
    focusOwnerBgColor: Color = Color.BlackBright,
    backgroundColor: Color = Color.Black,
    foregroundColor: Color = Color.White,
    keyStrokeFgColor : Color = Color.BlueBright
  ) extends MenuColorConfig