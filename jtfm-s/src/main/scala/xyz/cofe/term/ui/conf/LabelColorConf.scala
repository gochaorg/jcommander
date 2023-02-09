package xyz.cofe.term.ui.conf

import xyz.cofe.term.common.Color

trait LabelColorConf:
  def foreground: Color
  def background: Color

object LabelColorConf:
  case class Conf(
    foreground: Color = Color.Black,
    background: Color = Color.White,
  ) extends LabelColorConf
  
  implicit val defaultConf: Conf = Conf()
