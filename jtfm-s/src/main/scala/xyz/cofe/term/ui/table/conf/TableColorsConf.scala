package xyz.cofe.term.ui.table.conf

import xyz.cofe.term.common.Color

sealed trait TableColorsConf:
  def foreground:Color
  def background:Color
  def selectionBg:Color
  def selectionFg:Color
  def focusContainerBg:Color
  def focusContainerFg:Color
  def focusOwnerBg:Color
  def focusOwnerFg:Color

object TableColorsConf:
  val defaultColors : TableColorsConf = new Conf

  case class Conf(
    foreground:Color = Color.White,
    background:Color = Color.Black,
    selectionBg:Color = Color.BlackBright,
    selectionFg:Color = Color.RedBright,
    focusContainerBg:Color = Color.BlackBright,
    focusContainerFg:Color = Color.WhiteBright,
    focusOwnerBg:Color = Color.BlackBright,
    focusOwnerFg:Color = Color.YellowBright
  ) extends TableColorsConf
