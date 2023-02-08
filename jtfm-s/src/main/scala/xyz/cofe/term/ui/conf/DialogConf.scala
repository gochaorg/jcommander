package xyz.cofe.term.ui.conf

trait DialogConf:
  def restoreFocusAtClose:Boolean

object DialogConf:
  case class Conf(
    restoreFocusAtClose: Boolean = true
  ) extends DialogConf

  implicit val defaultConf : Conf = Conf()