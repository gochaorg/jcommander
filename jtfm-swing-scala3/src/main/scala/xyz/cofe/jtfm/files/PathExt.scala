package xyz.cofe.jtfm.files

import java.nio.file.Path
import java.nio.file.Files

import JavaNioOperation._
import scala.util.Try
import java.nio.file.DirectoryStream
import java.nio.charset.Charset

import xyz.cofe.jtfm.store.json.{FromJson,ToJson}
import xyz.cofe.jtfm.store.json.syntax._
import java.nio.charset.StandardCharsets
import java.io.IOException

extension (path:Path)
  def isDir(using trace:JavaNioTracer, lopt:LinkOptions):Either[Throwable,Boolean] = 
    val lOps = lopt.options
    val op = IsDirectory(path,lOps)
    try
      Right(trace(op)(Files.isDirectory(path, lOps:_* )))
    catch
      case e:Throwable => Left(trace.error(op)(e))

  def readDir(using trace:JavaNioTracer, lopt:LinkOptions):Either[Throwable,List[Path]] =
    val lOps = lopt.options
    val op = ReadDir(path, lOps)
    try
      Right(trace(op){
        val ds = Files.newDirectoryStream(path)
        val itr = ds.iterator()
        var ls = List[Path]()
        while itr.hasNext() do
          ls = itr.next() :: ls
        ds.close()
        ls.reverse
      })
    catch
      case e:Throwable => Left(trace.error(op)(e))

  def parent:Option[Path] =
    path.getParent() match
      case null => None
      case prnt:Path => Some(prnt)

  def name:String =
    path.getFileName().toString()

  def extension:Option[String] =
    val idx = name.lastIndexOf(".")
    if idx>=0 && idx<(name.length()-1) then
      Some(name.substring(idx+1))
    else
      None

  def exists(using trace:JavaNioTracer, lopt:LinkOptions):Either[Throwable,Boolean] =
    val lOps = lopt.options
    val op = Exists(path,lOps)
    try
      Right(trace(op)(Files.exists(path, lOps:_*)))
    catch
      case e:Throwable => Left(trace.error(op)(e))

  def isFile(using trace:JavaNioTracer, lopt:LinkOptions):Either[Throwable,Boolean] =
    val lOps = lopt.options
    val op = IsFile(path,lOps)
    try
      Right(trace(op)(Files.isRegularFile(path, lOps:_*)))
    catch
      case e:Throwable => Left(trace.error(op)(e))
    
  def canonical:Path =
    path.toAbsolutePath().normalize()

  def fileSize(using trace:JavaNioTracer):Either[Throwable,Long] =
    val op = FileSize(path)
    try
      Right(trace(op)(Files.size(path)))
    catch
      case e:Throwable => Left(trace.error(op)(e))

  def upPath:List[Path] = 
    var ls = List[Path]()
    var p = path.canonical
    var stop = false
    while !stop do
      ls = p :: ls
      p.parent match
        case Some(prnt) => p = prnt
        case None => stop = true
    ls

  def readString(cs:Charset)(using trace:JavaNioTracer):Either[Throwable,String] =
    val op = ReadString(path,cs)
    try
      Right(trace(op)(Files.readString(path,cs)))
    catch
      case e:Throwable => Left(trace.error(op)(e))

  def writeString(cs:Charset, str:String)(using trace:JavaNioTracer):Either[Throwable,Unit] =
    val op = WriteString(path,cs,str)
    try
      Right(
        trace(op) {
          val _ = Files.writeString(path, str, cs)
        }
      )
    catch
      case e:Throwable => Left(trace.error(op)(e))

  def readJson[T <: Product : FromJson](using trace:JavaNioTracer):Either[Throwable,T] = 
    for
      str <- readString(StandardCharsets.UTF_8)
      js <- str.parseJson[T].left.map( errMessage => new IOException(errMessage) )
    yield
      js

  def writeJson[T <: Product : ToJson](obj:T)(using trace:JavaNioTracer):Either[Throwable,Unit] = 
    for
      js <- obj.asJson.left.map( errMessage => new IOException(errMessage) )
      _  <- writeString(StandardCharsets.UTF_8, js)
    yield ()

  def createDirectories(using trace:JavaNioTracer):Either[Throwable,Unit] =
    val op = CreateDirectories(path)
    try
      Right(
        trace(op){ val _ = Files.createDirectories(path) }
      )
    catch
      case e:Throwable => Left(trace.error(op)(e))
