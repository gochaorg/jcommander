package xyz.cofe.jtfm
package conf

import MainMenu._
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.files.AppHome
import _root_.xyz.cofe.term.ui.menuBuilder.ActionConf
import xyz.cofe.json4s3.derv.FromJson
case class MainMenu(
  keyboard: List[KeyBinding]
):
  lazy val actionKeystrokeMap = 
    keyboard.map { kb =>
      (kb.action.name, kb.keyStroke)
    }.toMap

object MainMenu:
  case class KeyBinding( action:Main.Action, keyStroke:KeyStroke )

  def confFile(appHome:AppHome):ConfFile[MainMenu] =
    ConfFile.Fallback(
      ConfFile.File(appHome.directory.resolve("main-menu.jsonc")),
      ConfFile.Resource("/default-config/main-menu.jsonc")
    )

  def read(using appHome:AppHome):Either[ConfError,MainMenu] =
    confFile(appHome).read
