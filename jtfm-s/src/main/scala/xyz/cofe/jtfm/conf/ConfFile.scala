package xyz.cofe.jtfm.conf

import xyz.cofe.json4s3.derv.FromJson
import xyz.cofe.json4s3.derv._
import xyz.cofe.json4s3.derv.errors.DervError
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.charset.Charset
import java.io.StringWriter
import java.io.InputStream
import xyz.cofe.json4s3.derv.errors.TypeCastFail
import java.nio.file.Path
import xyz.cofe.files.readString
import xyz.cofe.files.writeString

enum ConfError(val message:String):
  case Except(val error:Throwable) extends ConfError(error.getMessage())
  case NotImplemented extends ConfError("not implemented")
  case NotSupported extends ConfError("not supported")
  case JsonError(val error:DervError) extends ConfError(error.getMessage())

trait ConfFile[A:FromJson]:
  def read:Either[ConfError,A]
  def write(inst:A):Either[ConfError,Unit]

object ConfFile:
  case class Resource[A:FromJson](resourceName:String) extends ConfFile[A]:
    override def read: Either[ConfError, A] = 
      Try(this.getClass().getResourceAsStream(resourceName)) match
        case Failure(exception) => Left(ConfError.Except(exception))
        case Success(inputStream) => 
          inputStream.streamAs[A].left.map( err => ConfError.JsonError(err) )

    override def write(inst: A): Either[ConfError, Unit] = Left(ConfError.NotSupported)

  case class File[A:FromJson:ToJson](path:Path) extends ConfFile[A]:
    override def read: Either[ConfError, A] = 
      path.readString(StandardCharsets.UTF_8)
        .left.map(ConfError.Except.apply)
        .flatMap { str => str.jsonAs[A].left.map(ConfError.JsonError.apply) }

    override def write(inst: A): Either[ConfError, Unit] = 
      path.writeString(inst.json, StandardCharsets.UTF_8)
        .left.map(ConfError.Except.apply)

  case class Fallback[A:FromJson:ToJson]( main:ConfFile[A], fallback:ConfFile[A] ) extends ConfFile[A]:
    override def read: Either[ConfError, A] = 
      main.read.orElse( fallback.read )

    override def write(inst: A): Either[ConfError, Unit] = 
      main.write(inst).orElse( fallback.write(inst) )
        

extension (inputStream:InputStream)
  def text(cs:Charset):Either[Throwable,String] =
    Try {
      val reader = InputStreamReader(inputStream,cs)
      val strWriter = new StringWriter
      reader.transferTo(strWriter)
      strWriter.toString()
    } match
      case Failure(exception) => Left(exception)
      case Success(value) => Right(value)
    
  def streamAs[A:FromJson]:Either[DervError,A] =
    try
      inputStream.text(StandardCharsets.UTF_8)
        .left.map(err => TypeCastFail(err.getMessage()))
        .flatMap { str =>
          str.jsonAs[A]
        }
    finally
      inputStream.close()