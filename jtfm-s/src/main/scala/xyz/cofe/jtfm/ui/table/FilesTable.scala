package xyz.cofe.jtfm.ui.table

import xyz.cofe.term.ui.table._
import xyz.cofe.files._
import java.nio.file.Path
import xyz.cofe.term.ui.Table

object FilesTable:
  def columns:List[Column[Path,_]] =
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