package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

trait DialogColorConf:
  def foreground: Color
  def background: Color
  def contentBg: Color
  def titleFg: Color

object DialogColorConf:
  case class Conf(
    foreground: Color = Color.Black,
    background: Color = Color.White,
    contentBg: Color  = Color.White,
    titleFg: Color = Color.Blue,
  ) extends DialogColorConf

  implicit val defaultConf : Conf = Conf()