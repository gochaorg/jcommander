package xyz.cofe.files

import java.nio.file.Path
import scala.annotation.tailrec

case class SymLink( file:Path, target:Path )(using log:FilesLogger, opts:FilesOption):
  def resolve:Either[SymLink.ResolveErr[Throwable],Path] = SymLink.resolve(
    file, 
    from => 
      from.isSymbolicLink.flatMap { isSym =>
        if isSym
        then Right(None)
        else from.readSymbolicLink.map { path => 
          Some(path)
        }
      }
  )

object SymLink:
  def from(file:Path)(using log:FilesLogger, opts:FilesOption):Either[Throwable,SymLink] =
    file.readSymbolicLink.map(target=>SymLink(file,target))

  enum ResolveErr[E]:
    case ReadLink(error:E) extends ResolveErr[E]
    case CycledLink[A,E](path:List[A]) extends ResolveErr[E]
    
  def resolve[A,E](target:A, readLink:A=>Either[E,Option[A]]):Either[ResolveErr[E],A] =
    readLink(target) match
      case Left(err) => Left(ResolveErr.ReadLink(err))
      case Right(linkOpt) => linkOpt match
        case None => Right(target)
        case Some(link) =>
          @tailrec
          def resolve_r(hist:List[A]):Either[ResolveErr[E],A] = 
            readLink(hist.head) match
              case Left(err) => Left(ResolveErr.ReadLink(err))
              case Right(linkOpt) => linkOpt match
                case None => Right(hist.head)
                case Some(nextLink) => 
                  if hist.contains(nextLink)
                  then Left(ResolveErr.CycledLink((nextLink :: hist).reverse))
                  else resolve_r(nextLink :: hist)            
          resolve_r(List(link))
    
