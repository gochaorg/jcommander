package xyz.cofe.jtfm.bg.copy

import java.nio.file.Path
import xyz.cofe.files._
import xyz.cofe.jtfm.ui.copy.CopyDialog.{CopyOption => DlgCopyOption}


class CopyTask(using 
  log:   CopyLog,
  flog:  FilesLogger, 
  opts:  FilesOption,
  copyStream: CopyStream
):
  // //TODO extract
  // def copy( copyCoption: DlgCopyOption ):Unit = 
  //   val target = copyCoption.target
  //   val targetIsDir = target.isDirectory.getOrElse(false)
  //   val fromTo = copyCoption.sourceFiles.filter(copyCoption.filter.apply).map { source =>
  //     if targetIsDir 
  //     then 
  //       if copyCoption.copyIntoFolder 
  //       then (source, target.resolve(source.name))
  //       else (source, target)
  //     else
  //       (source, target)
  //   }
  //   copy( fromTo, CopyOption.from(copyCoption) )

  def copy( fromTo:List[(Path,Path)], opt:CopyOption ):Option[CopyOption] =
    if fromTo.isEmpty then
      None
    else
      Iterator.iterate( (fromTo.head, Option(opt), fromTo.tail) ) {
        case ((from,to),Some(opt),tail) =>
          val s = copy(from,to,opt)
          tail
            .headOption.map { case (nextFrom,nextTo) => ((nextFrom,nextTo),s,tail.tail) }
            .getOrElse( ((from,to),None,tail) )
        case s @ ((from,to),None,tail) => s
      }.takeWhile { case (_, copt, _) => copt.isDefined }.toList.lastOption.flatMap( _._2 )

  def copy( from:Path, to:Path, opt:CopyOption ):Option[CopyOption] =
    log.copy(from,to){
      if from.isSymbolicLink.getOrElse(false)
      then copySymbolicLink(from,to,opt)
      else if from.isDirectory.getOrElse(false)
        then copyDirectory(from,to,opt)
        else if from.isRegularFile.getOrElse(false)
          then copyRegularFile(from,to,opt)
          else 
            log.notImplemented(s"undefined type of $from")
            Some(opt)
    }

  def copySymbolicLink( from:Path, to:Path, opt:CopyOption ):Option[CopyOption] =
    log.copySymbolicLink(from,to){
      log.notImplemented(s"copySymbolicLink '$from' to '$to'")
      Some(opt)
    }

  def copyDirectory( from:Path, to:Path, opt:CopyOption ):Option[CopyOption] =
    log.copyDirectory(from,to){
      log.notImplemented(s"copyDirectory '$from' to '$to'")
      Some(opt)
    }

  def copyRegularFile( from:Path, to:Path, opt:CopyOption ):Option[CopyOption] =
    log.copyRegularFile(from,to){
      to.exists match
        case Left(err)    => 
          log.error(s"exists of $to",err)
          None
        case Right(false) => copyStream.copy(from,to) match
          case Left(err) => 
            log.error(s"copy error from $from to $to",err)
            None
          case Right(value) => 
            Some(opt)
        case Right(true)  => 
          log.notImplemented(s"overwrite $to")
          Some(opt)
    }

