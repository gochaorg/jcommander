package xyz.cofe.jtfm
package conf

import MainMenu._
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.files.AppHome

case class MainMenu(
  keyboard: List[KeyStrokeBinding]
)

object MainMenu:
  case class KeyStrokeBinding(
    keyStroke: KeyStroke,
    actions: List[Main.Action]
  )

  def confFile(appHome:AppHome):ConfFile[MainMenu] =
    ConfFile.Fallback(
      ConfFile.File(appHome.directory.resolve("main-menu.jsonc")),
      ConfFile.Resource("/default-config/main-menu.jsonc")
    )

  def read(using appHome:AppHome):Either[ConfError,MainMenu] =
    confFile(appHome).read

      