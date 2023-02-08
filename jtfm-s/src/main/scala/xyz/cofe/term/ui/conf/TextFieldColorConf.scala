package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

trait TextFieldColorConf:
  def selectionFg: Color
  def selectionBg: Color
  def foreground: Color
  def background: Color

object TextFieldColorConf:
  case class Conf(
    selectionFg: Color = Color.White,
    selectionBg: Color = Color.Blue,
    foreground: Color = Color.White,
    background: Color = Color.Black,
  ) extends TextFieldColorConf
  implicit val defaultConf:Conf = Conf()
