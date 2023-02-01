package xyz.cofe.jtfm.conf

import xyz.cofe.files.AppHome
import xyz.cofe.term.ui.conf.MenuBarColorConfig
import xyz.cofe.term.ui.conf.MenuColorConfig
import xyz.cofe.term.ui.table.TableInputConf

class UiConf(using appHome:AppHome):
  val colorsConf:Either[ConfError,ColorsConf] = ColorsConf.read
  implicit val menuBarColors : MenuBarColorConfig.Conf = colorsConf.map(_.menu.bar).getOrElse(new MenuBarColorConfig.Conf)
  implicit val menuColors    : MenuColorConfig.Conf = colorsConf.map(_.menu.container).getOrElse(new MenuColorConfig.Conf)

  val tableConf:Either[ConfError,TableInputConf] = TableConf.read
  implicit val tableInputConf:TableInputConf = tableConf.map(x => x:TableInputConf).getOrElse(TableInputConf.defaultConfig)
