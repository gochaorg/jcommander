package xyz.cofe.jtfm.files

import java.nio.file.Path
import java.nio.file.LinkOption

sealed trait JavaNioOperation:
  type R
  
object JavaNioOperation {
  case class IsDirectory(path:Path, linkOptions:Seq[LinkOption]) extends JavaNioOperation { type R = Boolean }
  case class ReadDir(path:Path, linkOptions:Seq[LinkOption]) extends JavaNioOperation { type R = List[Path] }
  case class Exists(path:Path, linkOptions:Seq[LinkOption]) extends JavaNioOperation { type R = Boolean }
  case class IsFile(path:Path, linkOptions:Seq[LinkOption]) extends JavaNioOperation { type R = Boolean }
  case class FileSize(path:Path) extends JavaNioOperation { type R = Long }
}

trait JavaNioTracer:
  def apply[O <: JavaNioOperation](op:O)(code: => op.R):op.R
  def error[O <: JavaNioOperation,E <: Throwable](op:O)(error: E):E = error

object JavaNioTracer:
  given JavaNioTracer with
    def apply[O <: JavaNioOperation](op:O)(code: => op.R):op.R = code