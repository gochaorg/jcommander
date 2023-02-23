package xyz.cofe.jtfm.bg.copy

import java.nio.file.Path
import xyz.cofe.files._
import xyz.cofe.jtfm.ui.copy.CopyDialog.{CopyOption => DlgCopyOption}


class CopyTask(using log:CopyLog):
  //TODO extract
  def copy( copyCoption: DlgCopyOption ):Unit = 
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
    copy( fromTo, CopyOption.from(copyCoption) )

  def copy( fromTo:List[(Path,Path)], opt:CopyOption ):Unit =
    fromTo.foldLeft( opt ){ case(opt,(from,to)) => 
      copy(from,to,opt)
    }

  def copy( from:Path, to:Path, opt:CopyOption ):CopyOption =
    log.copy(from,to){
      if from.isSymbolicLink.getOrElse(false)
      then copySymbolicLink(from,to,opt)
      else if from.isDirectory.getOrElse(false)
        then copyDirectory(from,to,opt)
        else if from.isRegularFile.getOrElse(false)
          then copyRegularFile(from,to,opt)
          else 
            log.notImplemented(s"undefined type of $from")
            opt
    }

  def copySymbolicLink( from:Path, to:Path, opt:CopyOption ):CopyOption =
    log.copySymbolicLink(from,to){
      log.notImplemented(s"copySymbolicLink '$from' to '$to'")
    }
    opt

  def copyDirectory( from:Path, to:Path, opt:CopyOption ):CopyOption =
    log.copyDirectory(from,to){
      log.notImplemented(s"copyDirectory '$from' to '$to'")
    }
    opt

  def copyRegularFile( from:Path, to:Path, opt:CopyOption ):CopyOption =
    log.copyRegularFile(from,to){
      to.exists match
        case Left(err) => log.error(s"exists of $to",err)
        case Right(false) => 
        case Right(true) => 
    }
    opt

  def copyUndefined( from:Path, to:Path, opt:CopyOption ):CopyOption =
    log.notImplemented(s"copyUndefined '$from' to '$to'")
    opt

  // private def copyFile( from:Path, to:Path, opt:CopyOption ) =
  //   from.inputStream.