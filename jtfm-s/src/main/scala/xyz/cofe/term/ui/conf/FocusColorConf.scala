package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

trait FocusColorConfig:
  def focusContainerFgColor = Color.WhiteBright
  def focusOwnerFgColor = Color.YellowBright
  def focusContainerBgColor = Color.BlackBright
  def focusOwnerBgColor = Color.BlackBright
  def backgroundColor = Color.Black
  def foregroundColor = Color.White

object FocusColorConfig:
  given defaultColors: FocusColorConfig = new FocusColorConfig {}

  case class Conf(
    focusContainerFgColor: Color,
    focusOwnerFgColor: Color,
    focusContainerBgColor: Color,
    focusOwnerBgColor: Color,
    backgroundColor: Color,
    foregroundColor: Color
  )
