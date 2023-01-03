package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

trait FocusColorConfig:
  def focusContainerFgColor : Color
  def focusOwnerFgColor : Color
  def focusContainerBgColor : Color
  def focusOwnerBgColor : Color
  def backgroundColor : Color
  def foregroundColor : Color

object FocusColorConfig:
  given defaultColors: FocusColorConfig = new Conf()

  case class Conf(
    focusContainerFgColor: Color = Color.WhiteBright,
    focusOwnerFgColor: Color = Color.YellowBright,
    focusContainerBgColor: Color = Color.BlackBright,
    focusOwnerBgColor: Color = Color.BlackBright,
    backgroundColor: Color = Color.Black,
    foregroundColor: Color = Color.White
  ) extends FocusColorConfig
