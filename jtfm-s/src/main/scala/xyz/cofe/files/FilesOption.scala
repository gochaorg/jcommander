package xyz.cofe.files

import java.nio.file.LinkOption
import java.nio.file.attribute.FileAttribute
import java.nio.file.CopyOption
import java.nio.file.OpenOption
import xyz.cofe.json4s3.derv.ToJson
import xyz.cofe.json4s3.stream.ast.AST
import xyz.cofe.json4s3.stream.ast.AST._
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import xyz.cofe.json4s3.derv.FromJson
import xyz.cofe.json4s3.derv.errors.DervError
import xyz.cofe.json4s3.derv.errors.TypeCastFail
import xyz.cofe.json4s3.derv.errors.FieldNotFound

/**
  * Задает опции при работе с файловыми операциями
  */
trait FilesOption:
  def copy:FilesOption.Opts = FilesOption.Opts(
    linkOptions = linkOptions,
    fileAttributes = Seq(),
    copyOptions = copyOptions,
    openOptions = openOptions
  )
  def linkOptions:Seq[LinkOption]
  def fileAttributes:Seq[FileAttribute[_]]
  def copyOptions:Seq[CopyOption]
  def openOptions:Seq[OpenOption]

  def followLink:Boolean = 
    ! linkOptions.contains(LinkOption.NOFOLLOW_LINKS)

  def followLink(follow:Boolean):FilesOption =
    copy.copy(
      linkOptions = 
        if follow 
        then linkOptions.filterNot(_ == LinkOption.NOFOLLOW_LINKS) 
        else linkOptions :+ LinkOption.NOFOLLOW_LINKS
    )
  
object FilesOption:
  given defaultOption:FilesOption = new FilesOption {
    def linkOptions: Seq[LinkOption] = Seq()
    def fileAttributes: Seq[FileAttribute[_]] = Seq()
    def copyOptions: Seq[CopyOption] = Seq()
    def openOptions: Seq[OpenOption] = Seq()
  }

  case class Opts(
    linkOptions:Seq[LinkOption],
    fileAttributes:Seq[FileAttribute[_]],
    copyOptions:Seq[CopyOption],
    openOptions:Seq[OpenOption]
  ) extends FilesOption

  object Opts:
    import xyz.cofe.jtfm.json._
    given optsToJson:ToJson[Opts] with
      def toJson(opts: Opts): Option[AST] = Some({
        val linksAst = summon[ToJson[List[LinkOption]]].toJson(opts.linkOptions.toList)
        val copyAst = summon[ToJson[List[CopyOption]]].toJson(opts.copyOptions.toList)
        val openAst = summon[ToJson[List[OpenOption]]].toJson(opts.openOptions.toList)
        val filesAst = summon[ToJson[List[FileAttribute[_]]]].toJson(opts.fileAttributes.toList)
        JsObj(
          List(
            "link" -> linksAst,
            "copy" -> copyAst,
            "open" -> openAst,
            "file" -> filesAst,
          ).foldLeft(List[(String,AST)]()) { case (lst,(key,valOpt)) =>
            valOpt match
              case None => lst
              case Some(value) =>
                (key, value) :: lst
          }.reverse
        )
      })

    given optsFromJson:FromJson[Opts] with
      override def fromJson(j_ast: AST): Either[DervError, Opts] = 
        j_ast match
          case jsObj:JsObj =>
            val link1 = jsObj.get("link").map(link => 
              summon[FromJson[List[LinkOption]]].fromJson(link)
            ).getOrElse(Left(FieldNotFound("field 'link' not found")))

            val copy1 = jsObj.get("copy").map(link => 
              summon[FromJson[List[CopyOption]]].fromJson(link)
            ).getOrElse(Left(FieldNotFound("field 'copy' not found")))
            
            val open1 = jsObj.get("open").map(link => 
              summon[FromJson[List[OpenOption]]].fromJson(link)
            ).getOrElse(Left(FieldNotFound("field 'open' not found")))
            
            val file1 = jsObj.get("file").map(link => 
              summon[FromJson[List[FileAttribute[_]]]].fromJson(link)
            ).getOrElse(Left(FieldNotFound("field 'file' not found")))

            if link1.isRight && copy1.isRight && open1.isRight && file1.isRight
            then
              Opts(
                linkOptions = link1.getOrElse(List()),
                fileAttributes = file1.getOrElse(List()),
                copyOptions = copy1.getOrElse(List()),
                openOptions = open1.getOrElse(List())
              )

            Left(TypeCastFail(s"expect JsObj, but found $j_ast"))
          case _ =>
            Left(TypeCastFail(s"expect JsObj, but found $j_ast"))

    given linkOptionToJson:ToJson[LinkOption] with
      def toJson(link: LinkOption): Option[AST] = 
        Some(JsStr(link.name()))

    given linkOptionFromJson:FromJson[LinkOption] with
      override def fromJson(j: AST): Either[DervError, LinkOption] =
        summon[FromJson[String]]
          .fromJson(j)
          .flatMap{ str => 
            try
              Right(LinkOption.valueOf(str))
            catch
              case err:Throwable =>
                Left(TypeCastFail(s"can't cast LinkOption from $str: $err"))
          }


    given fileAttributesToJson:ToJson[FileAttribute[_]] with
      def toJson(fattribs: FileAttribute[_]): Option[AST] = None

    given fileAttributesFromJson:FromJson[FileAttribute[_]] with
      override def fromJson(j: AST): Either[DervError, FileAttribute[_]] =
        Left(TypeCastFail("not impl"))

    given copyOptionsToJson:ToJson[CopyOption] with
      def toJson(opt:CopyOption):Option[AST] = 
        opt match
          case linkOpt:LinkOption => Some(JsStr(s"lo:${linkOpt.name()}"))
          case stdOpt:StandardCopyOption =>
            Some(JsStr(s"sco:${stdOpt.name()}"))
          case _ =>
            None
    given copyOptionsFromJson:FromJson[CopyOption] with
      override def fromJson(j: AST): Either[DervError, CopyOption] = 
        summon[FromJson[String]]
          .fromJson(j)
          .flatMap{ str => 
            try
              if str.startsWith("lo:")
              then Right(LinkOption.valueOf(str.substring(3)))
              else
                if str.startsWith("sco:")
                then Right(StandardCopyOption.valueOf(str.substring(4)))
                else Left(TypeCastFail(s"can't cast CopyOption from $str, not found prefix sco: | lo:"))
            catch
              case err:Throwable =>
                Left(TypeCastFail(s"can't cast CopyOption from $str: $err"))
          }


    given openOptionsToJson:ToJson[OpenOption] with
      def toJson(opt: OpenOption): Option[AST] = 
        opt match
          case linkOpt:LinkOption => Some(JsStr(s"lo:${linkOpt.name()}"))
          case stdOpt:StandardOpenOption =>
            Some(JsStr(s"soo:${stdOpt.name()}"))
          case _ =>
            None
    given openOptionsFromJson:FromJson[OpenOption] with
      override def fromJson(j: AST): Either[DervError, OpenOption] = 
        summon[FromJson[String]]
          .fromJson(j)
          .flatMap{ str => 
            try 
              if str.startsWith("lo:")
              then Right(LinkOption.valueOf(str.substring(3)))
              else
                if str.startsWith("soo:")
                then Right(StandardOpenOption.valueOf(str.substring(4)))
                else Left(TypeCastFail(s"can't cast OpenOption from $str, not found prefix soo: | lo:"))
            catch
              case err:Throwable =>
                Left(TypeCastFail(s"can't cast OpenOption from $str: $err"))
          }
