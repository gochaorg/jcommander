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
import xyz.cofe.files.readDir
import xyz.cofe.files.name

class Copy[S:MkDirS:FileTypeS:CopyFileS:CopySymLinkS]( using
  log:        FilesLogger, 
  opts:       FilesOption,
  cancel:     CancelSignal,
  listener:   CopyStreamListener,
  bufferSize: BufferSize,
):
  private val stopFlag = AtomicBoolean(false)
  cancel.listen { stopFlag.set(true) }

  def copy( state:S, from:Path, to:Path ):Either[S,S] = 
    copyRecusive.copy( (from,to), state )

  private given nested:Nested[(Path,Path)] with
    override def hasNested(fromTo: (Path, Path)): Boolean = 
      val (from,to) = fromTo
      from.isDirectory.getOrElse(false)

    override def nestedOf(fromTo: (Path, Path)): List[(Path, Path)] = 
      val (from,to) = fromTo
      from.readDir.map { fromList =>  
        fromList.map { from =>
          (from, to.resolve(from.name))
        }
      }.getOrElse(List.empty)

  private lazy val copyFileStream: CopyStream = new CopyStream()
  private def copyFile( fromTo:(Path,Path), state:S ):Either[S,S] = 
    val (from,to) = fromTo
    from.isRegularFile match
      case Left(err) => 
        summon[FileTypeS[S]].isRegularFileFail(state,from)
      case Right(isFile) =>
        if isFile 
        then copyFileStream.copy(from,to) match
          case Left(err) => 
            summon[CopyFileS[S]].copyFail(state,from,to,err)
          case Right(_) =>
            summon[CopyFileS[S]].copySucc(state,from,to)
        else SymLink.from(from) match
          case Right(symLink) =>
            CopyLink.copyAsLinkAbsolute(symLink,to) match
              case Left(err) => 
                summon[CopySymLinkS[S]].copyFail(state,from,to,err)
              case Right(_) =>
                summon[CopySymLinkS[S]].copySucc(state,from,to)
          case Left(_readSymLinkErr) => 
            from.isDirectory match
              case Left(err) => 
                summon[FileTypeS[S]].isDirFail(state,from)
              case Right(isDir) =>
                if isDir then
                  summon[FileTypeS[S]].unexpectDirFileType(state,from)
                else
                  summon[FileTypeS[S]].undefinedFileType(state,from)

  private def mkdir( fromTo:(Path,Path), state:S ):Either[S,S] = 
    val (from,to) = fromTo
    to.createDirectories() match
      case Left(err) => 
        summon[MkDirS[S]].mkdirFail(state,to,err)
      case Right(value) =>
        summon[MkDirS[S]].mkdirSucc(state,to)

  private lazy val copyRecusive: CopyRecursive[(Path,Path),S] = CopyRecursive(
    copyFile,
    mkdir
  )
