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
import xyz.cofe.json4s3.derv.ToJson

trait FilesLogger:
  def apply[R](op:FilesOperation)(code: => R):Either[Throwable,R] =
    try
      Right(code)
    catch
      case err:IOError => Left(err)
      case err:IOException => Left(err)

object FilesLogger:
  given defaultLogger:FilesLogger = new FilesLogger {}

enum FilesOperation:
  case IsDirectory(path:Path, opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Boolean]
  case IsRegularFile(path:Path, opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Boolean]
  case LastModified(path:Path, opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Instant]
  case SetLastModified(path:Path, time:Instant ) //  extends FilesOperation with OperationResult.OutResult[Unit]
  case Size(path:Path) // extends FilesOperation with OperationResult.OutResult[Long]
  case Exists(path:Path, opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Boolean]
  case ReadDir(path:Path) // extends FilesOperation with OperationResult.OutResult[List[Path]]
  case IsSameFile(path:Path,other:Path) // extends FilesOperation with OperationResult.OutResult[Boolean]
  case IsSymbolicLink(path:Path) // extends FilesOperation with OperationResult.OutResult[Boolean]

  case CreateDirectories(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Unit]
  case CreateFile(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Unit]
  case CreateLink(path:Path,target:Path) // extends FilesOperation with OperationResult.OutResult[Unit]
  case CreateSymbolicLink(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Unit]
  case CreateTempDirectory(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Path]
  case CreateTempFile(path:Path,opts:FilesOption.Opts) //extends FilesOperation with OperationResult.OutResult[Path]
  case Delete(path:Path) // extends FilesOperation with OperationResult.OutResult[Unit]
  case DeleteifExists(path:Path) // extends FilesOperation with OperationResult.OutResult[Boolean]
  case Move(path:Path,target:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Unit]
  case ReadSymbolicLink​(path:Path) // extends FilesOperation with OperationResult.OutResult[Path]
  case Channel(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[SeekableByteChannel]
  case InputStreamOp(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[InputStream]
  case OutputStreamOp(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[OutputStream]
  case ReadString(path:Path,charset:Charset) // extends FilesOperation with OperationResult.OutResult[String]
  case WriteString(path:Path,charset:Charset,string:String,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Unit]
  case ReadBytes(path:Path) // extends FilesOperation with OperationResult.OutResult[Array[Byte]]
  case PosixPerm(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[PosixPerm]
  // case SetPosixPerm(path:Path,perm:PosixPerm) // extends FilesOperation with OperationResult.OutResult[Unit]

object FilesOperation:
  given timeToJson:ToJson[Instant] = xyz.cofe.jtfm.json.instantToJson