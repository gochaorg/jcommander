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
import xyz.cofe.json4s3.stream.ast.AST
import xyz.cofe.json4s3.derv.FromJson
import xyz.cofe.json4s3.derv.errors.DervError
import xyz.cofe.json4s3.derv.errors.TypeCastFail
import xyz.cofe.json4s3.derv.errors.FieldNotFound
import java.nio.charset.StandardCharsets

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
  case WriterOp(path:Path, charset:Charset, opts:FilesOption.Opts)
  case ReaderOp(path:Path, charset:Charset)
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
  case DeleteIfExists(path:Path) // extends FilesOperation with OperationResult.OutResult[Boolean]
  case Move(path:Path,target:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Unit]
  case ReadSymbolicLink​(path:Path) // extends FilesOperation with OperationResult.OutResult[Path]
  case Channel(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[SeekableByteChannel]
  case InputStreamOp(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[InputStream]
  case OutputStreamOp(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[OutputStream]
  case ReadString(path:Path,charset:Charset) // extends FilesOperation with OperationResult.OutResult[String]
  case WriteString(path:Path,charset:Charset,string:String,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[Unit]
  case ReadBytes(path:Path) // extends FilesOperation with OperationResult.OutResult[Array[Byte]]
  // case PosixPerm(path:Path,opts:FilesOption.Opts) // extends FilesOperation with OperationResult.OutResult[PosixPerm]
  // case SetPosixPerm(path:Path,perm:PosixPerm) // extends FilesOperation with OperationResult.OutResult[Unit]
  

object FilesOperation:
  import xyz.cofe.jtfm.json.charsetToJson
  import xyz.cofe.jtfm.json.charsetFromJson
  import xyz.cofe.files.given
  given ToJson[WriteString] with
    override def toJson(ws: WriteString): Option[AST] = 
      val path1 = summon[ToJson[Path]].toJson(ws.path)
      val cs1 = summon[ToJson[Charset]].toJson(ws.charset)
      val ops1 = summon[ToJson[FilesOption.Opts]].toJson(ws.opts)
      Some(
        AST.JsObj(
          List(
            "path" -> path1,
            "charset" -> cs1,
            "opts" -> ops1,
            "string" -> Some(AST.JsStr(ws.string)),
          ).foldLeft(List[(String,AST)]()) { case (lst,(key,valOpt)) =>
            valOpt match
              case None => lst
              case Some(value) =>
                (key, value) :: lst
          }.reverse
        )
      )
  given FromJson[WriteString] with
    override def fromJson(j_ast: AST): Either[DervError, WriteString] = 
      j_ast match
          case jsObj:AST.JsObj =>
            val path1 = jsObj.get("path").map(js => 
              summon[FromJson[Path]].fromJson(js)
            ).getOrElse(Left(FieldNotFound("field 'path' not found")))

            val cs1 = jsObj.get("charset").map(js => 
              summon[FromJson[Charset]].fromJson(js)
            ).getOrElse(Left(FieldNotFound("field 'charset' not found")))

            val opts1 = jsObj.get("opts").map(js => 
              summon[FromJson[FilesOption.Opts]].fromJson(js)
            ).getOrElse(Left(FieldNotFound("field 'opts' not found")))

            val str1 = jsObj.get("string").map(js => 
              summon[FromJson[String]].fromJson(js)
            ).getOrElse(Left(FieldNotFound("field 'string' not found")))

            if path1.isRight && cs1.isRight && opts1.isRight && str1.isRight
            then
              Right(WriteString(
                path1.getOrElse(Path.of(".")),
                cs1.getOrElse(StandardCharsets.UTF_8),
                str1.getOrElse(""),
                opts1.getOrElse(FilesOption.defaultOption.copy)
              ))
            else
              Left(TypeCastFail(s"can't cast"))

          case _ =>
            Left(TypeCastFail(s"expect JsObj, but found $j_ast"))