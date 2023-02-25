package xyz.cofe.files

import java.nio.file.Path

/**
  * Определяет тип файла/каталога
  */
enum FileType:
  case RegularFile
  case Directory
  case SymbolicLink

object FileType:  
  object UndefinedType extends Error

  def from(path: Path):Either[Throwable,FileType] =
    from_basicImpl(path)

  private def from_basicImpl(path:Path):Either[Throwable,FileType] =
    path.isRegularFile.flatMap {
      case true => Right(FileType.RegularFile)
      case false => path.isSymbolicLink.flatMap {
        case true => Right(FileType.SymbolicLink)
        case false => path.isDirectory.flatMap {
          case true => Right(FileType.Directory)
          case false => Left(UndefinedType)
        }
      }
    }