package xyz.cofe.files

import java.nio.file.Path
import java.nio.file.Files
import java.time.Instant

import FilesOperation._
import java.nio.file.attribute.{FileTime => JFileTime}
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
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.FileSystems

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

  def fileTime:Either[Throwable,FileTime] = {
    log(GetFileTime(path,opts.copy)){
      val attr = Files.readAttributes(path,classOf[BasicFileAttributes],opts.linkOptions:_*)
      FileTime(
        attr.creationTime().toInstant(),
        attr.lastAccessTime().toInstant(),
        attr.lastModifiedTime().toInstant()
      )
    }
  }

  def setFileTime( fileTime:FileTime ) =
    log(SetFileTime(path,fileTime,opts.copy)){
      val attrView = Files.getFileAttributeView(path, classOf[BasicFileAttributeView], opts.linkOptions:_*)
      attrView.setTimes(
        JFileTime.from(fileTime.lastModified),
        JFileTime.from(fileTime.lastAccess),
        JFileTime.from(fileTime.creation)
      )
    }

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

  def createDirectory():Either[Throwable,Unit] =
    log(CreateDirectory(path,opts.copy)) {
      Files.createDirectory(path,opts.fileAttributes:_*)
    }

  def createFile:Either[Throwable,Unit] =
    log(CreateFile(path,opts.copy)) {
      Files.createFile(path,opts.fileAttributes:_*)
    }
  def createLink(target:Path):Either[Throwable,Unit] =
    log(CreateLink(path,target)) {
      Files.createLink(path,target)
    }
  def createSymbolicLink(target:Path):Either[Throwable,Path] =
    log(CreateSymbolicLink(path,target,opts.copy)){
      Files.createSymbolicLink(path,target,opts.fileAttributes:_*)
    }
  def setOwner(ownerName:String):Either[Throwable,Unit] =
    log(SetOwner(path,ownerName,opts.copy)){
      val lookupService = FileSystems.getDefault().getUserPrincipalLookupService()
      val usr = lookupService.lookupPrincipalByName(ownerName)
      val attrView = Files.getFileAttributeView(
        path, classOf[PosixFileAttributeView], opts.linkOptions:_*)
      attrView.setOwner(usr)
    }
  def setGroup(groupName:String):Either[Throwable,Unit] =
    log(SetGroup(path,groupName,opts.copy)){
      val lookupService = FileSystems.getDefault().getUserPrincipalLookupService()
      val grp = lookupService.lookupPrincipalByGroupName(groupName)
      val attrView = Files.getFileAttributeView(
        path, classOf[PosixFileAttributeView], opts.linkOptions:_*)
      attrView.setGroup(grp)
    }

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
  def readSymbolicLink:Either[Throwable,Path] = {
    log(ReadSymbolicLink(path)){ 
      Files.readSymbolicLink(path)
    }
  }
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

  def setPosixPerm(perm:PosixPerm):Either[Throwable,Path] =
    log(SetPosixPerm(path,perm)){
      Files.setPosixFilePermissions(path,perm.posixPerm)
    }

  def posixAttributes:Either[Throwable,PosixAttib] =
    log(GetPosixAttib(path,opts.copy)){
      val cls:Class[PosixFileAttributeView] = classOf[PosixFileAttributeView]
      val attr = Files.getFileAttributeView(path,cls,opts.linkOptions:_*)
      val posixAttr = attr.readAttributes()
      PosixAttib(
        posixAttr.owner().getName(),
        posixAttr.group().getName(),
        PosixPerm(
          posixAttr.permissions().contains(PosixFilePermission.OWNER_READ),
          posixAttr.permissions().contains(PosixFilePermission.OWNER_WRITE),
          posixAttr.permissions().contains(PosixFilePermission.OWNER_EXECUTE),

          posixAttr.permissions().contains(PosixFilePermission.GROUP_READ),
          posixAttr.permissions().contains(PosixFilePermission.GROUP_WRITE),
          posixAttr.permissions().contains(PosixFilePermission.GROUP_EXECUTE),

          posixAttr.permissions().contains(PosixFilePermission.OTHERS_READ),
          posixAttr.permissions().contains(PosixFilePermission.OTHERS_WRITE),
          posixAttr.permissions().contains(PosixFilePermission.OWNER_EXECUTE),
        )
      )
    }

  def walk:TreeWalk = new TreeWalk(List(path))

  def isRoot:Boolean =
    path.isAbsolute() && path.getParent()==null

case class FileTime(
  creation: Instant,
  lastAccess: Instant,
  lastModified: Instant
)
case class PosixAttib(
  owner: String,
  group: String,
  perm: PosixPerm
)

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
):
  lazy val rwxString:String =
    List(
      if ownerRead     then "r" else "-",
      if ownerWrite    then "w" else "-",
      if ownerExecute  then "x" else "-",
      if groupRead     then "r" else "-",
      if groupWrite    then "w" else "-",
      if groupExecute  then "x" else "-",
      if othersRead    then "r" else "-",
      if othersWrite   then "w" else "-",
      if othersExecute then "x" else "-",
    ).mkString
  lazy val posixPerm = {
    val set = new java.util.HashSet[PosixFilePermission]()
    if ownerRead     then set.add(PosixFilePermission.OWNER_READ)
    if ownerWrite    then set.add(PosixFilePermission.OWNER_WRITE)
    if ownerExecute  then set.add(PosixFilePermission.OWNER_EXECUTE)
    if groupRead     then set.add(PosixFilePermission.GROUP_READ)
    if groupWrite    then set.add(PosixFilePermission.GROUP_WRITE)
    if groupExecute  then set.add(PosixFilePermission.GROUP_EXECUTE)
    if othersRead    then set.add(PosixFilePermission.OTHERS_READ)
    if othersWrite   then set.add(PosixFilePermission.OTHERS_WRITE)
    if othersExecute then set.add(PosixFilePermission.OTHERS_EXECUTE)
    set
  }

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

