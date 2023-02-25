package xyz.cofe.jtfm.bg.copy

import xyz.cofe.files.FilesLogger
import xyz.cofe.files.FilesOption
import java.nio.file.Path
import xyz.cofe.files.createDirectories
import monix.execution.atomic.AtomicBoolean
import xyz.cofe.files.isRegularFile
import xyz.cofe.files.isSymbolicLink
import xyz.cofe.files.SymLink
import xyz.cofe.files.isDirectory

class Copy[S:MkDirS]( using
  log:        FilesLogger, 
  opts:       FilesOption,
  cancel:     CancelSignal,
  listener:   CopyStreamListener,
  bufferSize: BufferSize,
):
  private val stopFlag = AtomicBoolean(false)
  cancel.listen { stopFlag.set(true) }

  def copy( from:Path, to:Path ):Unit = ???

  implicit lazy val nested:Nested[(Path,Path)] = ???

  lazy val copyFileStream: CopyStream = new CopyStream()
  def copyFile( fromTo:(Path,Path), state:S ):Option[S] = 
    val (from,to) = fromTo
    from.isRegularFile.flatMap { isFile =>
      if isFile 
      then copyFileStream.copy(from,to).map( _ => Option(state))
      else SymLink.from(from) match
        case Left(err) => 
          from.isDirectory.flatMap{ isDir =>
            if isDir then
              Right(())
            else
              Right(())
          }
        case Right(symLink) =>
          CopyLink.copyAsLinkAbsolute(symLink,to).map( _ => Option(state))
    }
    ???

  def mkdir( fromTo:(Path,Path), state:S ):Option[S] = 
    val (from,to) = fromTo
    to.createDirectories() match
      case Left(err) => 
        summon[MkDirS[S]].mkdirFail(state,to,err)
      case Right(value) =>
        summon[MkDirS[S]].mkdirSucc(state,to)

  lazy val copyRecusive: CopyRecursive[(Path,Path),S] = CopyRecursive(
    copyFile,
    mkdir
  )
