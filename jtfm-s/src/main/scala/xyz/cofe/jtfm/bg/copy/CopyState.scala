package xyz.cofe.jtfm.bg.copy

import java.nio.file.Path

trait MkDirS[S]:
  def mkdirFail(s:S, dir:Path, err:Throwable):Option[S]
  def mkdirSucc(s:S, dir:Path):Option[S]

trait FileTypeS[S]:
  def undefinedFileType(s:S, file:Path):Option[S]
