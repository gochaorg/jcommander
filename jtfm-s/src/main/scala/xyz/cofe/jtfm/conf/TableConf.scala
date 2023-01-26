package xyz.cofe.jtfm.conf

import xyz.cofe.files.AppHome
import xyz.cofe.term.ui.table.TableInputConf

object TableConf:
  def confFile(appHome:AppHome):ConfFile[TableInputConf.Conf] =
    ConfFile.Fallback(
      ConfFile.File(appHome.directory.resolve("table/input.jsonc")),
      ConfFile.Resource("/default-config/table/input.jsonc")
    )

  def read(using appHome:AppHome):Either[ConfError,TableInputConf.Conf] =
    confFile(appHome).read
