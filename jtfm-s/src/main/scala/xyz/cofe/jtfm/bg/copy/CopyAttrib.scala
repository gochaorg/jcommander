package xyz.cofe.jtfm.bg.copy

import java.nio.file.Path
import xyz.cofe.files._

object CopyAttrib:
  def copyTimes(from:Path, to:Path)(using log:FilesLogger, opts:FilesOption):Either[Throwable,Unit] =
    from.fileTime.flatMap { ftime =>
      to.setFileTime(ftime)
    }
  def copyPosixOwner(from:Path, to:Path)(using log:FilesLogger, opts:FilesOption):Either[Throwable,Unit] =
    from.posixAttributes.flatMap { attrib =>
      to.setOwner(attrib.owner)
    }
  def copyPosixGroup(from:Path, to:Path)(using log:FilesLogger, opts:FilesOption):Either[Throwable,Unit] =
    from.posixAttributes.flatMap { attrib =>
      to.setGroup(attrib.group)
    }
  def copyPosixPerm(from:Path, to:Path)(using log:FilesLogger, opts:FilesOption):Either[Throwable,Unit] =
    from.posixAttributes.flatMap { attrib =>
      to.setPosixPerm(attrib.perm).map(_ => ())
    }
  def copyAttrib(from:Path, to:Path)(using log:FilesLogger, opts:FilesOption):Either[Throwable,Unit] =
    for {
      _ <- copyTimes(from,to)
      _ <- copyPosixPerm(from,to)
      _ <- copyPosixGroup(from,to)
      _ <- copyPosixOwner(from,to)
    } yield ()
