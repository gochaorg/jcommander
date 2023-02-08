package xyz.cofe.jtfm.conf

import xyz.cofe.files.AppHome
import xyz.cofe.term.ui.conf.MenuBarColorConfig
import xyz.cofe.term.ui.conf.MenuColorConfig
import xyz.cofe.term.ui.table.conf.TableInputConf
import xyz.cofe.term.ui.table.conf.TableColorsConf
import xyz.cofe.term.ui.conf.DialogConf
import xyz.cofe.term.ui.conf.DialogColorConf
import xyz.cofe.term.ui.conf.LabelColorConf
import xyz.cofe.term.ui.conf.ButtonColorConf

class UiConf(using appHome:AppHome):
  val colorsConf:Either[ConfError,ColorsConf] = ColorsConf.read
  implicit val menuBarColors : MenuBarColorConfig.Conf = colorsConf.map(_.menu.bar).getOrElse(new MenuBarColorConfig.Conf)
  implicit val menuColors    : MenuColorConfig.Conf = colorsConf.map(_.menu.container).getOrElse(new MenuColorConfig.Conf)
  
  val mainMenuConf:Either[ConfError,MainMenu] = MainMenu.read
  implicit val mainMenu: MainMenu = mainMenuConf.getOrElse(new MainMenu(List()))

  val tableConf:Either[ConfError,TableInputConf] = TableConf.read
  implicit val tableInputConf:TableInputConf   = tableConf.map(x => x:TableInputConf).getOrElse(TableInputConf.defaultConfig)
  implicit val tableColorsConf:TableColorsConf = colorsConf.map(_.table).getOrElse(TableColorsConf.defaultColors)

  implicit val dialogConf: DialogConf = DialogConf.defaultConf
  implicit val dialogColorsConf: DialogColorConf = DialogColorConf.defaultConf

  implicit val labelColorsConf: LabelColorConf = LabelColorConf.defaultConf
  implicit val buttonColorsConf: ButtonColorConf = ButtonColorConf.defaultConf
