package xyz.cofe.jtfm.json

import xyz.cofe.json4s3.derv.ToJson
import xyz.cofe.json4s3.stream.ast.AST
import xyz.cofe.json4s3.stream.ast.AST._
import java.time.Instant
import java.nio.charset.Charset
import java.time.format.DateTimeFormatter
import xyz.cofe.json4s3.derv.FromJson
import xyz.cofe.json4s3.derv.errors.DervError
import xyz.cofe.json4s3.derv.errors.TypeCastFail

given instantToJson:ToJson[Instant] with
  val df = DateTimeFormatter.ISO_INSTANT
  def toJson(time: Instant): Option[AST] = 
    Some(JsStr(df.format(time)))

given instantFromJson:FromJson[Instant] with
  override def fromJson(j: AST): Either[DervError, Instant] = 
    summon[FromJson[String]]
      .fromJson(j)
      .flatMap{ str => 
        try 
          Right(Instant.parse(str))
        catch
          case err:Throwable =>
            Left(TypeCastFail(s"can't parse Instant from '$str': $err"))
      }

given charsetToJson:ToJson[Charset] with
  def toJson(cs: Charset): Option[AST] = Some(JsStr(cs.name()))

given charsetFromJson:FromJson[Charset] with
  def fromJson(j: AST): Either[DervError, Charset] = 
    summon[FromJson[String]]
      .fromJson(j)
      .flatMap{ str => 
        try 
          Right(Charset.forName(str))
        catch
          case err:Throwable =>
            Left(TypeCastFail(s"can't parse Instant from '$str': $err"))
      }
