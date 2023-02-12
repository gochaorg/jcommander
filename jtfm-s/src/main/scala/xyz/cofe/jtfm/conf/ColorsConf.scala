package xyz.cofe.jtfm.conf

import ColorsConf._
import xyz.cofe.term.common.Color
import xyz.cofe.term.ui.conf.MenuBarColorConfig
import xyz.cofe.term.ui.conf.MenuColorConfig
import xyz.cofe.files.AppHome
import xyz.cofe.term.ui.conf.given
import xyz.cofe.term.ui.table.conf.TableColorsConf
import xyz.cofe.term.ui.conf._
import xyz.cofe.jtfm.ui.table.conf.DirectoryTableColorConf

case class ColorsConf(
  menu:  MenuColors,
  table: TableColorsConf,
  button: ButtonColorConf,
  dialog: DialogColorConf,
  directoryTable: DirectoryTableColorConf,
)

object ColorsConf:
  case class MenuColors (
    bar : MenuBarColorConfig.Conf,
    container : MenuColorConfig.Conf
  )

  def confFile(appHome:AppHome):ConfFile[ColorsConf] =
    ConfFile.Fallback(
      ConfFile.File(appHome.directory.resolve("colors.jsonc")),
      ConfFile.Resource("/default-config/colors.jsonc")
    )

  def read(using appHome:AppHome):Either[ConfError,ColorsConf] =
    confFile(appHome).read

