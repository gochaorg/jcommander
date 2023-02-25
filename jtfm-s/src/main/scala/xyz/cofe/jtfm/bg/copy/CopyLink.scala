package xyz.cofe.jtfm.bg.copy

import xyz.cofe.files._
import java.nio.file.Path
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object CopyLink:
  def copyAsIs( source:SymLink, target:Path )(using log:FilesLogger, opts:FilesOption):Either[Throwable,Unit] =
    target.createSymbolicLink(source.target).map(_ => ())

  def copyAsLinkAbsolute( source:SymLink, target:Path )(using log:FilesLogger, opts:FilesOption):Either[Throwable,Unit] =
    Try { 
      source.file.toRealPath()
    } match
      case Failure(exception) => Left(exception)
      case Success(realPath) => 
        target.createSymbolicLink(realPath).map(_ => ())

  def copyAsLinkRelative( source:SymLink, target:Path )(using log:FilesLogger, opts:FilesOption):Either[Throwable,Unit] =
    Try { 
      source.file.toRealPath()
    } match
      case Failure(exception) => Left(exception)
      case Success(realPath) => 
        Try { 
          target.relativize(realPath)
        } match
          case Failure(exception) => Left(exception)
          case Success(linkTarget) =>
            target.createSymbolicLink(linkTarget).map(_ => ())

  def copyContent[R]( source:SymLink, target:Path, doCopy:(Path,Path)=>R )(using log:FilesLogger, opts:FilesOption):Either[Throwable,R] =
    Try { 
      source.file.toRealPath()
    } match
      case Failure(exception) => Left(exception)
      case Success(realPath) => 
        Right(doCopy(realPath, target))
        