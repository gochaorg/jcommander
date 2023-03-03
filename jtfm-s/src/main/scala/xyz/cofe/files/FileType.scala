package xyz.cofe.files

import java.nio.file.Path
import xyz.cofe.files.jnr.Posix
import xyz.cofe.files.jnr.PosixResult
import java.io.IOException
import _root_.jnr.ffi.Platform.OS

/**
  * Определяет тип файла/каталога
  */
enum FileType:
  case RegularFile
  case Directory
  case SymbolicLink
  case NamedPipe
  case Character
  case Block
  case Socket

object FileType:  
  object UndefinedType extends Error
  class PosixIOError( val err:PosixResult.Errors ) extends IOException
  class PosixIOTypeError( val err:jnr.FileStat.FileStatError ) extends IOException

  def from(path: Path)(using filesOpts:FilesOption):Either[Throwable,FileType] =
    from_jnrOpt.map { 
      fjnr => 
        fjnr(path,filesOpts)
          .orElse( from_basicImpl(path,filesOpts) )
    }.getOrElse( from_basicImpl(path,filesOpts) )

  private def from_basicImpl(path:Path, filesOpts:FilesOption):Either[Throwable,FileType] =
    path.isRegularFile.flatMap {
      case true => Right(FileType.RegularFile)
      case false => path.isSymbolicLink.flatMap {
        case true => Right(FileType.SymbolicLink)
        case false => path.isDirectory.flatMap {
          case true => Right(FileType.Directory)
          case false => Left(UndefinedType)
        }
      }
    }

  private def from_jnrImpl(path:Path, filesOpts:FilesOption):Either[Throwable,FileType] =
    Posix.stat( path, filesOpts.followLink )
      .toEither.left.map( err => new PosixIOError(err) )
      .flatMap { stat => 
        stat.fileTypeSafe
          .left.map { e => new PosixIOTypeError(e) }
          .map {            
              case xyz.cofe.files.jnr.FileType.NamedPipe => FileType.NamedPipe
              case xyz.cofe.files.jnr.FileType.Character => FileType.Character
              case xyz.cofe.files.jnr.FileType.Directory => FileType.Directory
              case xyz.cofe.files.jnr.FileType.Block     => FileType.Block
              case xyz.cofe.files.jnr.FileType.Regular   => FileType.RegularFile
              case xyz.cofe.files.jnr.FileType.SymLink   => FileType.SymbolicLink
              case xyz.cofe.files.jnr.FileType.Socket    => FileType.Socket
          }
      }

  private lazy val from_jnrOpt =
    val NATIVE_PLATFORM = _root_.jnr.ffi.Platform.getNativePlatform();
    NATIVE_PLATFORM.getOS() match
      case OS.LINUX => Some(from_jnrImpl)
      case OS.DARWIN => Some(from_jnrImpl)
      //case OS.FREEBSD =>
      //case OS.NETBSD =>
      //case OS.OPENBSD =>
      //case OS.DRAGONFLY =>
      //case OS.SOLARIS =>
      //case OS.WINDOWS =>
      //case OS.AIX =>
      //case OS.IBMI =>
      //case OS.ZLINUX =>
      //case OS.MIDNIGHTBSD =>
      case _ => None
    