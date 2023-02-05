package xyz.cofe.term.ui.ses.conf

trait DialogConf:
  def restoreFocusAtClose:Boolean

object DialogConf:
  given DialogConf with
    override def restoreFocusAtClose: Boolean = true