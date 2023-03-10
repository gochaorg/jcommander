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
import xyz.cofe.json4s3.derv._
import xyz.cofe.json4s3.derv.given
import xyz.cofe.files.given
import org.slf4j.LoggerFactory
import org.slf4j.Logger

trait FilesLogger:
  def apply[R](op:FilesOperation)(code: => R):Either[Throwable,R] =
    try
      Right(code)
    catch
      case err:IOError => Left(err)
      case err:IOException => Left(err)

object FilesLogger:
  given defaultLogger:FilesLogger = new FilesLogger {}
  def slf(logger:Logger, succ:Level, fail:Level):FilesLogger =
    new FilesLogger {
      override def apply[R](op: FilesOperation)(code: => R): Either[Throwable, R] = 
        try
          val res = code
          succ.write(logger,op.toJson)
          Right(res)
        catch
          case err:IOError => 
            fail.write(logger,op.toJson,err)
            Left(err)
          case err:IOException => 
            fail.write(logger,op.toJson,err)
            Left(err)
    }

  enum Level:
    case Warn,Info,Debug,Trace,Error
    def write(lgr:Logger, message:String):Unit =
      this match
        case Warn =>  lgr.warn (message)
        case Info =>  lgr.info (message)
        case Debug => lgr.debug(message)
        case Trace => lgr.trace(message)
        case Error => lgr.error(message)
    def write(lgr:Logger, message:String, err:Throwable):Unit =
      this match
        case Warn =>  lgr.warn (message,err)
        case Info =>  lgr.info (message,err)
        case Debug => lgr.debug(message,err)
        case Trace => lgr.trace(message,err)
        case Error => lgr.error(message,err)
      

enum FilesOperation:
  case WriterOp(path:Path, charset:Charset, opts:FilesOption.Opts)
  case ReaderOp(path:Path, charset:Charset)
  case IsDirectory(path:Path, opts:FilesOption.Opts)
  case IsRegularFile(path:Path, opts:FilesOption.Opts)
  case Size(path:Path)
  case Exists(path:Path, opts:FilesOption.Opts) 
  case ReadDir(path:Path) 
  case IsSameFile(path:Path,other:Path) 
  case IsSymbolicLink(path:Path)

  case CreateDirectory(path:Path,opts:FilesOption.Opts)
  case CreateDirectories(path:Path,opts:FilesOption.Opts)
  case CreateFile(path:Path,opts:FilesOption.Opts) 
  case CreateLink(path:Path,target:Path) 
  case CreateSymbolicLink(path:Path,target:Path,opts:FilesOption.Opts) 
  case Delete(path:Path)
  case DeleteIfExists(path:Path)
  case Move(path:Path,target:Path,opts:FilesOption.Opts)
  case ReadSymbolicLink???(path:Path) 
  case Channel(path:Path,opts:FilesOption.Opts) 
  case InputStreamOp(path:Path,opts:FilesOption.Opts) 
  case OutputStreamOp(path:Path,opts:FilesOption.Opts) 
  case ReadString(path:Path,charset:Charset) 
  case WriteString(path:Path,charset:Charset,string:String,opts:FilesOption.Opts) 
  case ReadBytes(path:Path)
  case GetPosixAttib(path:Path,optsz:FilesOption.Opts)
  case SetPosixPerm(path:Path, perm:PosixPerm)
  case ReadSymbolicLink(path:Path)
  case GetFileTime(path:Path,opts:FilesOption.Opts)
  case SetFileTime(path:Path,time:FileTime,opts:FilesOption.Opts)
  case SetOwner(path:Path,owner:String,opts:FilesOption.Opts)
  case SetGroup(path:Path,owner:String,opts:FilesOption.Opts)

  def toJson:String = FilesOperation.toJson(this)

object FilesOperation:
  import xyz.cofe.jtfm.json.charsetToJson
  import xyz.cofe.jtfm.json.charsetFromJson
  import xyz.cofe.files.given
  import xyz.cofe.jtfm.json.instantFromJson
  import xyz.cofe.jtfm.json.instantToJson
  import xyz.cofe.files.FilesOption.Opts.optsToJson
  import xyz.cofe.files.FilesOption.Opts.optsFromJson
  import xyz.cofe.files.pathFromJson
  import xyz.cofe.files.pathToJson
  import xyz.cofe.json4s3.derv.FromJson.given

  val x = summon[ToJson[FilesOperation.WriterOp]]

  def fromJson(jsonString:String) = 
    jsonString.jsonAs[FilesOperation]

  def toJson(op:FilesOperation):String =
    summon[ToJson[FilesOperation]].toJson(op).map(_.json).get

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