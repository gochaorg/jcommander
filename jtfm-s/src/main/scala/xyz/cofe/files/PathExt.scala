package xyz.cofe.files

import java.nio.file.Path
import java.nio.file.Files
import java.time.Instant

import FilesOperation._
import java.nio.file.attribute.FileTime
import java.nio.channels.SeekableByteChannel
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import xyz.cofe.json4s3.derv.ToJson
import xyz.cofe.json4s3.stream.ast.AST
import java.io.BufferedWriter
import java.io.BufferedReader
import xyz.cofe.json4s3.derv.FromJson
import xyz.cofe.json4s3.derv.errors.DervError
import xyz.cofe.json4s3.derv.errors.TypeCastFail.apply
import xyz.cofe.json4s3.derv.errors.TypeCastFail

extension (path:Path)(using log:FilesLogger, opts:FilesOption)
  def name: String = path.getFileName.toString
  def extension: Option[String] = 
    val idx = name.lastIndexOf(".")
    if (idx >= 0 && idx < (name.length() - 1))
      Some(name.substring(idx + 1))
    else
      None
  def parent: Option[Path] = {
      path.getParent match {
        case null => None
        case prnt: Path => Some(prnt)
      }
    }

  def canonical: Path = path.toAbsolutePath.normalize()

  def upPath: List[Path] = {
      var ls = List[Path]()
      var p = path.canonical
      var stop = false
      while (!stop) {
        ls = p :: ls
        p.parent match {
          case Some(prnt) => p = prnt
          case None => stop = true
        }
      }
      ls
    }

  def isDirectory:Either[Throwable,Boolean] = 
    log(IsDirectory(path,opts.copy)) { Files.isDirectory(path,opts.linkOptions:_*) }
  def isRegularFile:Either[Throwable,Boolean] = 
    log(IsRegularFile(path,opts.copy)) { Files.isRegularFile(path,opts.linkOptions:_*) }
  def lastModified:Either[Throwable,Instant] =
    log(LastModified(path,opts.copy)) { Files.getLastModifiedTime(path,opts.linkOptions:_*).toInstant() }
  def setLastModified(time:Instant):Either[Throwable,Unit] =
    log(SetLastModified(path,time)) { Files.setLastModifiedTime(path,FileTime.from(time)) }
  def size:Either[Throwable,Long] = 
    log(Size(path)) { Files.size(path) }
  def exists:Either[Throwable,Boolean] = 
    log(Exists(path,opts.copy)) { Files.exists(path,opts.linkOptions:_*) }
  def readDir:Either[Throwable,List[Path]] =
    log(ReadDir(path)) {
      var files = List[Path]()
      val ds = Files.newDirectoryStream(path)
      ds.forEach( p => files = p :: files )
      ds.close()
      files
    }
  def isSameFile(path2:Path):Either[Throwable,Boolean] =
    log(IsSameFile(path,path2)) { Files.isSameFile(path,path2) }

  def isSymbolicLink:Either[Throwable,Boolean] =
    log(IsSymbolicLink(path)){ Files.isSymbolicLink(path) }

  def createDirectories():Either[Throwable,Unit] =
    log(CreateDirectories(path,opts.copy)) {
      Files.createDirectories(path,opts.fileAttributes:_*)
    }
  def createFile:Either[Throwable,Unit] =
    log(CreateFile(path,opts.copy)) {
      Files.createFile(path,opts.fileAttributes:_*)
    }
  def createLink(target:Path):Either[Throwable,Unit] =
    log(CreateLink(path,target)) {
      Files.createLink(path,target)
    }
  // def createSymbolicLink(target:Path):Either[Throwable,Unit]
  // def createTempDirectory(prefix:String):Either[Throwable,Path]
  // def createTempFile(prefix:String,suffix:String):Either[Throwable,Path]
  def delete():Either[Throwable,Unit] =
    log(Delete(path)) {
      Files.delete(path)
    }
  def deleteIfExists():Either[Throwable,Boolean] = 
    log(DeleteIfExists(path)) {
      Files.deleteIfExists(path)
    }
  def move(target:Path):Either[Throwable,Unit] = 
    log(Move(path,target,opts.copy)) {
      Files.move(path,target,opts.copyOptions:_*)
    }
  // def readSymbolicLink​:Either[Throwable,Path]
  // def channel:Either[Throwable,SeekableByteChannel]
  def inputStream:Either[Throwable,InputStream] =
    log(InputStreamOp(path,opts.copy)) {
      Files.newInputStream(path,opts.openOptions:_*)
    }
  def outputStream:Either[Throwable,OutputStream] =
    log(OutputStreamOp(path,opts.copy)) {
      Files.newOutputStream(path,opts.openOptions:_*)
    }
  def readString(cs:Charset):Either[Throwable,String] =
    log(ReadString(path,cs)) {
      Files.readString(path,cs)
    }
  def writeString(string:String,cs:Charset):Either[Throwable,Unit] =
    log(WriteString(path,cs,string,opts.copy)) {
      Files.writeString(path,string,cs,opts.openOptions:_*)
    }

  def writer(cs:Charset):Either[Throwable,BufferedWriter] = 
    log(WriterOp(path,cs,opts.copy)) {
      Files.newBufferedWriter(path,cs,opts.openOptions:_*)
    }

  def reader(cs:Charset):Either[Throwable,BufferedReader] =
    log(ReaderOp(path,cs)) {
      Files.newBufferedReader(path,cs)
    }

  // def readBytes:Either[Throwable,Array[Byte]]
  // def writeBytes(bytes:Array[Byte]):Either[Throwable,Unit]
  // def posixPerm:Either[Throwable,PosixPerm]
  // def setPosixPerm(perm:PosixPerm):Either[Throwable,Unit]

  def walk:TreeWalk = new TreeWalk(List(path))
    

case class PosixPerm(
  ownerRead:Boolean,
  ownerWrite:Boolean,
  ownerExecute:Boolean,

  groupRead:Boolean,
  groupWrite:Boolean,
  groupExecute:Boolean,

  othersRead:Boolean,
  othersWrite:Boolean,
  othersExecute:Boolean,
)

given pathToJson:ToJson[Path] with
  def toJson(path: Path): Option[AST] = 
    Some(AST.JsStr(path.toString()))

given pathFromJson:FromJson[Path] with
  override def fromJson(j: AST): Either[DervError, Path] = 
    summon[FromJson[String]]
    .fromJson(j)
    .flatMap { str =>
      try 
        Right(Path.of(str))
      catch
        case err:Throwable =>
          Left(TypeCastFail(s"can't cast to Path from $str: $err"))
    }

