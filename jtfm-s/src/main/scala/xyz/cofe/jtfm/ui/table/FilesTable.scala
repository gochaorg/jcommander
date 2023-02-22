package xyz.cofe.jtfm.ui.table

import xyz.cofe.term.ui.table._
import xyz.cofe.files._
import java.nio.file.Path
import xyz.cofe.term.ui.Table
import java.time.Instant
import timeRender.shortCellEitherValue

object FilesTable:
  def allColumns:List[Column[Path,_]] =
    val cols = new FilesColumns()
    List(
      cols.fileTypeLetterColumn,
      cols.nameColumn,
      cols.rwxColumn,
      cols.ownerColumn,
      cols.groupColumn,
      cols.lastModifyColumn,
      cols.sizeHumanReadableColumn,
    )

  def allColumnsMap:Map[String,Column[Path,_]] =
    allColumns.map( c => c.id -> c ).toMap

  def defaultColumns:List[Column[Path,_]] =
    val cols = new FilesColumns()
    List(
      cols.fileTypeLetterColumn,
      cols.nameColumn,
      cols.rwxColumn,
      cols.sizeHumanReadableColumn,
    )

  class FilesColumns:
    val nameColumn = Column
      .id("file.name")
      .reader { (path:Path) => path.name }
      .text( text => text )
      .title("name")
      .widthAuto
      .leftAlign
      .build

    val sizeHumanReadableColumn = Column
      .id("file.size.h")
      .reader { (path:Path) => path.size }
      .text { etSize => etSize.map(sz => ByteSize(sz).humanReadable.toString()).getOrElse("?") }
      .title("size")
      .width(8)
      .build

    val fileTypeLetterColumn = Column
      .id("file.type.letter")
      .reader { (path:Path) => 
        if path.isSymbolicLink.getOrElse(false) then "~"
        else if path.isRegularFile.getOrElse(false) then " "
        else if path.isDirectory.getOrElse(false) then "/"
        else "?"
      }
      .text( text => text )
      .title(" ")
      .width(1)
      .leftAlign
      .leftDelimNone
      .rightDelimNone
      .build

    val rwxColumn = Column
      .id("file.rwx")
      .reader { (path:Path) => 
        path.posixAttributes.map(_.perm.rwxString).getOrElse("?")
      }
      .text( text => text )
      .title("perm")
      .width(9)
      .build

    val lastModifyColumn = Column
      .id("file.lastModify")
      .extract { (path:Path) => path.fileTime.map(_.lastModified) }
      .title("last mod")
      .width(10)
      .build

    val ownerColumn = Column
      .id("file.owner")
      .reader { (path:Path) => path.posixAttributes.map(_.owner).getOrElse("?") }
      .text( txt => txt )
      .title("owner")
      .width(8)
      .build

    val groupColumn = Column
      .id("file.group")
      .reader { (path:Path) => path.posixAttributes.map(_.group).getOrElse("?") }
      .text( txt => txt )
      .title("group")
      .width(8)
      .build

  object sort:
    val directoryFirst:Ordering[Path] = new Ordering[Path] {
      override def compare(x: Path, y: Path): Int = 
        x.isDirectory.flatMap { xIsDir => 
          y.isDirectory.map { yIsDir =>
            if xIsDir == yIsDir then 0
            else if xIsDir then -1
              else 1
          }
        }.getOrElse(0)
    }

    val nameAsc:Ordering[Path] = new Ordering[Path] {
      override def compare(x: Path, y: Path): Int = 
        x.name.compareToIgnoreCase( y.name )
    }

    val oneDotNameFirst:Ordering[Path] = new Ordering[Path] {
      override def compare(x: Path, y: Path): Int = 
        if x.name == y.name then 0
        else if x.name == "." then -1
        else if y.name == "." then 1
        else 0
    }

    val twoDotNameFirst:Ordering[Path] = new Ordering[Path] {
      override def compare(x: Path, y: Path): Int = 
        if x.name == y.name then 0
        else if x.name == ".." then -1
        else if y.name == ".." then 1
        else 0
    }

    def combine( ords:List[Ordering[Path]] ) = new Ordering[Path] {
      override def compare(x: Path, y: Path): Int = 
        ords.foldLeft( 0 ){ case (r,ord) => 
          if r!=0 then r
          else ord.compare(x,y)
        }
    }

    val defaultSort = combine(List(
      oneDotNameFirst, 
      twoDotNameFirst,
      directoryFirst,
      nameAsc
    ))

    def sort( list:List[Path], sortFun:Ordering[Path] ) =
      list.sorted(sortFun)

    def apply( list:List[Path], sortFun:Ordering[Path] ) = sort(list, sortFun)