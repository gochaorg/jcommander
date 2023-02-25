package xyz.cofe.jtfm.bg.copy

import java.nio.file.Path

trait MkDirS[S]:
  def mkdirFail(s:S, dir:Path, err:Throwable):Either[S,S]
  def mkdirSucc(s:S, dir:Path):Either[S,S]

trait FileTypeS[S]:
  def undefinedFileType(s:S, file:Path):Either[S,S]
  def unexpectDirFileType(s:S, file:Path):Either[S,S]
  def isDirFail(s:S, file:Path):Either[S,S] = Right(s)
  def isRegularFileFail(s:S, file:Path):Either[S,S] = Right(s)

trait CopyFileS[S]:
  def copyFail(s:S, from:Path, to:Path, err:Throwable):Either[S,S]
  def copySucc(s:S, from:Path, to:Path):Either[S,S]

trait CopySymLinkS[S]:
  def copyFail(s:S, from:Path, to:Path, err:Throwable):Either[S,S]
  def copySucc(s:S, from:Path, to:Path):Either[S,S]