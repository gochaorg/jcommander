package xyz.cofe.jtfm.bg.copy

import java.nio.file.Path
import xyz.cofe.jtfm.ui.copy.CopyDialog.CopyOption
import xyz.cofe.files._

object CopyTask:
  def copy( copyCoption: CopyOption ):Unit = 
    val target = copyCoption.target
    val targetIsDir = target.isDirectory.getOrElse(false)
    val fromTo = copyCoption.sourceFiles.filter(copyCoption.filter.apply).map { source =>
      if targetIsDir 
      then 
        if copyCoption.copyIntoFolder 
        then (source, target.resolve(source.name))
        else (source, target)
      else
        (source, target)
    }
    ???

  def copy( fromTo:List[(Path,Path)] ):Unit =
    ???