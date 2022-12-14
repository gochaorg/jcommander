package xyz.cofe.files

import java.nio.file.Path
import java.io.IOError
import java.io.IOException
import java.io.IOError
import java.time.Instant
import java.nio.channels.SeekableByteChannel
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

trait FilesLogger:
  def apply(op:FilesOperation)(code: => op.Out):Either[Throwable,op.Out] =
    try
      Right(code)
    catch
      case err:IOError => Left(err)
      case err:IOException => Left(err)

object FilesLogger:
  given defaultLogger:FilesLogger = new FilesLogger {}

trait OperationResult:
  type Out

object OperationResult:
  trait BoolResult extends OperationResult { type Out = Boolean }
  trait OutResult[O] extends OperationResult { type Out = O }

enum FilesOperation extends OperationResult:
  case IsDirectory(path:Path, opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[Boolean]
  case IsRegularFile(path:Path, opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[Boolean]
  case LastModified(path:Path, opts:FilesOption.Opts)  extends FilesOperation with OperationResult.OutResult[Instant]
  case SetLastModified(path:Path, time:Instant)  extends FilesOperation with OperationResult.OutResult[Unit]
  case Size(path:Path) extends FilesOperation with OperationResult.OutResult[Long]
  case Exists(path:Path, opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[Boolean]
  case ReadDir(path:Path) extends FilesOperation with OperationResult.OutResult[List[Path]]
  case IsSameFile(path:Path,other:Path) extends FilesOperation with OperationResult.OutResult[Boolean]
  case IsSymbolicLink(path:Path) extends FilesOperation with OperationResult.OutResult[Boolean]

  case CreateDirectories(path:Path,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[Unit]
  case CreateFile(path:Path,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[Unit]
  case CreateLink(path:Path) extends FilesOperation with OperationResult.OutResult[Unit]
  case CreateSymbolicLink(path:Path,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[Unit]
  case CreateTempDirectory(path:Path,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[Path]
  case CreateTempFile(path:Path,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[Path]
  case Delete(path:Path) extends FilesOperation with OperationResult.OutResult[Unit]
  case DeleteifExists(path:Path) extends FilesOperation with OperationResult.OutResult[Boolean]
  case Move(path:Path,target:Path,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[Unit]
  case ReadSymbolicLink​(path:Path) extends FilesOperation with OperationResult.OutResult[Path]
  case Channel(path:Path,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[SeekableByteChannel]
  case InputStreamOp(path:Path,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[InputStream]
  case OutputStreamOp(path:Path,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[OutputStream]
  case ReadString(path:Path,charset:Charset) extends FilesOperation with OperationResult.OutResult[String]
  case WriteString(path:Path,charset:Charset,string:String,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[Unit]
  case ReadBytes(path:Path) extends FilesOperation with OperationResult.OutResult[Array[Byte]]
  case PosixPerm(path:Path,opts:FilesOption.Opts) extends FilesOperation with OperationResult.OutResult[PosixPerm]
  case SetPosixPerm(path:Path,perm:PosixPerm) extends FilesOperation with OperationResult.OutResult[Unit]
