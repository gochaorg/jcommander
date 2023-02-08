package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

trait ButtonColorConf:
  def foreground: Color
  def background: Color
  def toLabelColorConf: LabelColorConf = LabelColorConf.Conf(foreground, background)

object ButtonColorConf:
  case class Conf(
    foreground: Color = Color.White,
    background: Color = Color.Blue,
  ) extends ButtonColorConf

  val defaultConf: Conf = Conf()
