package xyz.cofe.files
package log

import java.nio.file.Path
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.io.Writer
import xyz.cofe.files.log.PathPattern.Evaluate

class AppendableFile(
  pathPattern: ()=>Path,
  charset: Charset=StandardCharsets.UTF_8,
  limitSizePerFile:Option[Long]=None
)(using options:FilesOption, log:FilesLogger) 
extends java.lang.Appendable with AutoCloseable:
  @volatile private var closed: Boolean = false
  @volatile private var writer: Option[Writer] = None
  @volatile private var path: Option[Path] = None

  private def open() = {
    val path = pathPattern()
    path.canonical.parent.foreach { dir => dir.createDirectories() }
    this.path = Some(path)
    path.writer(charset).map { w =>
      writer = Some(w)
      w
    }
  }

  private def newFile = {
    writer match {
      case Some(w) =>
        w.close()
        open()
      case None =>
        open()
    }
  }

  private def write( code: Writer => Unit ):Unit = {
    if( !closed ) {
      writer match {
        case Some(w) =>
          code(w)
          w.flush()
          path.foreach { curFile =>
            limitSizePerFile.foreach { limitSize =>
              curFile.size.foreach { curSize =>
                if( curSize>limitSize ){
                  newFile.left.map { e => throw e }
                }
              }
            }
          }
        case None =>
          newFile.left.map { e => throw e }.map { w =>
            code(w)
            w.flush()
          }
      }
    }
  }

  override def append(csq: CharSequence): Appendable = {
    this.synchronized {
      write { w =>
        w.append(csq)
      }
      this
    }
  }

  override def append(csq: CharSequence, start: Int, end: Int): Appendable = {
    this.synchronized {
      write { w =>
        w.append(csq,start,end)
      }
    }
    this
  }
  override def append(c: Char): Appendable = {
    this.synchronized {
      write { w =>
        w.append(c)
      }
    }
    this
  }

  override def close(): Unit = {
    this.synchronized {
      writer match {
        case Some(w) =>
          w.close()
          closed = true
        case None =>
      }
    }
  }


object AppendableFile {
  def apply(pathPattern: List[PathPattern.Name],
            limitSizePerFile: Option[Long] = None,
            charset: Charset = StandardCharsets.UTF_8,
           )(implicit
             opts: FilesOption,
             logs: FilesLogger,
             evaluate: Evaluate
             )
  :AppendableFile = new AppendableFile(
    ()=> {
      pathPattern.generate match {
        case Left(err) => throw new Error(s"fail generate log path from $pathPattern: $err")
        case Right(value) => value
      }
    }, charset, limitSizePerFile
  )
}