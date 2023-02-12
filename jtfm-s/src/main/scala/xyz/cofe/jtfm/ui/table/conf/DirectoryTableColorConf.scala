package xyz.cofe.jtfm.ui.table.conf

import xyz.cofe.term.ui.conf.ButtonColorConf
import xyz.cofe.term.common.Color

case class DirectoryTableColorConf(
  directoryLabel: ButtonColorConf
)

object DirectoryTableColorConf:
  val defaultConf = DirectoryTableColorConf(
    directoryLabel = ButtonColorConf.Conf(
      foreground = Color.Black,
      background = Color.White
    )
  )