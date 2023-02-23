package xyz.cofe.jtfm.bg.copy

import xyz.cofe.jtfm.ui.copy.CopyDialog.{CopyOption => DlgCopyOption}

case class CopyOption(
  saveAttrib:Boolean, 
  resolveSymLinks:ResolveSymLink,
  blockSize:Int = 1024*64,
  overwrite:Overwrite = Overwrite.Ask
)
object CopyOption:
  def from(dlgCopyOption:DlgCopyOption):CopyOption =
    CopyOption(
      saveAttrib = dlgCopyOption.saveAttribs,
      resolveSymLinks = dlgCopyOption.resolveSymLinks,
    )

enum Overwrite:
  case Ask
  case Skip
  case Always

