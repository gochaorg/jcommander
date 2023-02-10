package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

sealed trait DialogColorConf:
  def foreground: Color
  def background: Color
  def contentBg: Color
  def titleFg: Color
  def closeButton: ButtonColorConf

object DialogColorConf:
  case class Conf(
    foreground: Color = Color.Black,
    background: Color = Color.White,
    contentBg: Color  = Color.White,
    titleFg: Color = Color.Blue,
    closeButton: ButtonColorConf = 
      ButtonColorConf.defaultConf.copy(
        foreground = Color.RedBright,
        background = Color.White
      )
  ) extends DialogColorConf

  implicit val defaultConf : Conf = Conf()