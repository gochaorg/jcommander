package xyz.cofe.jtfm.conf

import ColorsConf._
import xyz.cofe.term.common.Color
import xyz.cofe.term.ui.conf.MenuBarColorConfig
import xyz.cofe.term.ui.conf.MenuColorConfig
import xyz.cofe.files.AppHome
import xyz.cofe.term.ui.conf.given

case class ColorsConf(
  menu: MenuColors
)

object ColorsConf:
  case class MenuColors (
    bar : MenuBarColorConfig.Conf,
    container : MenuColorConfig.Conf
  )

  def confFile(appHome:AppHome):ConfFile[ColorsConf] =
    ConfFile.Fallback(
      ConfFile.File(appHome.directory.resolve("colors.json")),
      ConfFile.Resource("/default-config/colors.json")
    )

