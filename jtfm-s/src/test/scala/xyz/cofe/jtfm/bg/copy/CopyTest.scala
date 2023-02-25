package xyz.cofe.jtfm.bg.copy

import xyz.cofe.files._
import java.nio.file.Path
import java.util.concurrent.ThreadLocalRandom

class CopyTest extends munit.FunSuite:
  val testDir = Path.of("target/test/CopyTest")
  val srcDir = testDir.resolve("src")
  val trgtDir = testDir.resolve("target")

  test("prepare") {
    println("prepare")
    println("="*60)
    
    implicit val log = FilesLogger.output(System.out)

    def recurDelete(path:Path):Unit =
      if path.isDirectory.getOrElse(false) 
      then
        path.readDir.getOrElse(List.empty).foreach( recurDelete )
        path.delete()
        ()
      else
        path.deleteIfExists()
        ()

    def touch(path:Path, size:Int) =
      val rnd = ThreadLocalRandom.current()
      path.parent.foreach( dir => 
        if ! dir.exists.getOrElse(true)
        then dir.createDirectories()
      )
      path.outputStream.foreach(strm =>
        val buff = new Array[Byte](size)
        rnd.nextBytes(buff)
        strm.write(buff)
        strm.close()
      )

    recurDelete(srcDir)
    recurDelete(trgtDir)

    if ! srcDir.exists.getOrElse(false) then println(srcDir.createDirectories())
    touch( srcDir.resolve("a"), 512 )
    touch( srcDir.resolve("b/a"), 1024 )
    touch( srcDir.resolve("b/b/a"), 1024 )
    srcDir.resolve("c").createDirectories()
  }

  case class TestState()
  given MkDirS[TestState] with
    override def mkdirFail(s: TestState, dir: Path, err: Throwable) = 
      println(s"!! mkdir $dir $err")
      Right(s)

    override def mkdirSucc(s: TestState, dir: Path) = 
      println(s"ok mkdir $dir")
      Right(s)

  given FileTypeS[TestState] with
    override def undefinedFileType(s: TestState, file: Path) = 
      Right(s)
    override def unexpectDirFileType(s: TestState, file: Path) = 
      Right(s)

  given CopyFileS[TestState] with
    override def copyFail(s: TestState, from: Path, to: Path, err: Throwable) = 
      println(s"!! copy file $from $to $err")
      Right(s)
    override def copySucc(s: TestState, from: Path, to: Path) = 
      println(s"ok copy file $from $to")
      Right(s)

  given CopySymLinkS[TestState] with
    override def copyFail(s: TestState, from: Path, to: Path, err: Throwable) = 
      println(s"!! copy link $from $to $err")
      Right(s)
    override def copySucc(s: TestState, from: Path, to: Path) = 
      println(s"ok copy link $from $to")
      Right(s)

  test("copy") {
    println("copy")
    println("="*60)

    //implicit val log = FilesLogger.output(System.out)
    implicit val sign : CancelSignal = CancelSignal.CancelSignalImpl()
    implicit val bufSize = BufferSize(1024)

    val res = Copy().copy( TestState(), srcDir, trgtDir )
    println(s"result $res")
  }