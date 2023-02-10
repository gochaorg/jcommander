package xyz.cofe.jtfm.conf

import xyz.cofe.files.AppHome
import xyz.cofe.term.ui.conf.MenuBarColorConfig
import xyz.cofe.term.ui.conf.MenuColorConfig
import xyz.cofe.term.ui.table.conf.TableInputConf
import xyz.cofe.term.ui.table.conf.TableColorsConf
import xyz.cofe.term.ui.conf._
import xyz.cofe.log._
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UiConf(using appHome:AppHome):
  private implicit val logger : Logger = LoggerFactory.getLogger("xyz.cofe.jtfm.conf.UiConf")
  lazy val colorsConf:Either[ConfError,ColorsConf] = {
    val colors = ColorsConf.read
    colors match
      case Left(err) =>    warn"colors conf error $err"
      case Right(value) => ()
    
    colors
  }
  implicit lazy val menuBarColors : MenuBarColorConfig.Conf = colorsConf.map(_.menu.bar).getOrElse(new MenuBarColorConfig.Conf)
  implicit lazy val menuColors    : MenuColorConfig.Conf = colorsConf.map(_.menu.container).getOrElse(new MenuColorConfig.Conf)
  
  lazy val mainMenuConf:Either[ConfError,MainMenu] = MainMenu.read
  implicit lazy val mainMenu: MainMenu = mainMenuConf.getOrElse(new MainMenu(List()))

  lazy val tableConf:Either[ConfError,TableInputConf] = TableConf.read
  implicit lazy val tableInputConf:TableInputConf   = tableConf.map(x => x:TableInputConf).getOrElse(TableInputConf.defaultConfig)
  implicit lazy val tableColorsConf:TableColorsConf = colorsConf.map(_.table).getOrElse(TableColorsConf.defaultColors)

  implicit lazy val dialogConf: DialogConf = DialogConf.defaultConf
  implicit lazy val dialogColorsConf: DialogColorConf = colorsConf.map(_.dialog).getOrElse(DialogColorConf.defaultConf)

  implicit lazy val labelColorsConf: LabelColorConf = LabelColorConf.defaultConf
  implicit lazy val buttonColorsConf: ButtonColorConf = colorsConf.map(_.button).getOrElse(ButtonColorConf.defaultConf)
  implicit lazy val textFieldColorsConf: TextFieldColorConf = TextFieldColorConf.defaultConf

  implicit lazy val leftRightDirs: LeftRightDirs = LeftRightDirs.read.getOrElse(LeftRightDirs.defaultConf)
