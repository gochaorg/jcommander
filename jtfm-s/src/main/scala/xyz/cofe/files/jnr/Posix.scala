package xyz.cofe.files.jnr

import java.nio.file.Path
import java.time.Instant

object Posix:
  /** Чтение информации о файле */
  def stat( path:Path, followLink:Boolean ):PosixResult[FileStat] =
    PosixHandler.use { hdl => 
      if followLink
      then hdl.posix.stat(path.toAbsolutePath().normalize().toString())
      else hdl.posix.lstat(path.toAbsolutePath().normalize().toString())
    }.map { fileStat => 
      FileStat(
        FileStat.ModeFlags(fileStat.mode()),
        UserID(fileStat.uid()),
        GroupID(fileStat.gid()),
        accessTime = Instant.ofEpochSecond(fileStat.atime()),
        modifyTime = Instant.ofEpochSecond(fileStat.mtime()),
        createTime = Instant.ofEpochSecond(fileStat.ctime())
      )
    }

  def chmod( path:Path, perm:FilePerm, followLink:Boolean ):PosixResult[Int] =
    PosixHandler.use { hdl =>
      if followLink
      then hdl.posix.chmod( path.toString(), perm.modeFlags.intValue )
      else hdl.posix.lchmod( path.toString(), perm.modeFlags.intValue )
    }

  def chown( path:Path, usr:UserID, grp:GroupID, followLink:Boolean ):PosixResult[Int] =
    PosixHandler.use { hdl =>
      if followLink
      then hdl.posix.chown( path.toString(), usr.usrValue, grp.grpValue )
      else hdl.posix.lchown( path.toString(), usr.usrValue, grp.grpValue )
    }