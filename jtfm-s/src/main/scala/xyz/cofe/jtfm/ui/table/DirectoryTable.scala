package xyz.cofe.jtfm.ui.table

import xyz.cofe.term.ui.Table
import java.nio.file.Path
import xyz.cofe.lazyp.Prop
import xyz.cofe.term.ui.Session
import xyz.cofe.files._
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.term.common.KeyName
import xyz.cofe.files.parent
import xyz.cofe.files.parent
import xyz.cofe.files.parent

class DirectoryTable(using conf:DirectoryTableConf)
extends Table[Path]:
  val directory = Prop.rw(None:Option[Path])
  directory.onChange(refresh)
  columns.append(FilesTable.columns)

  val order = Prop.rw(Some(FilesTable.sort.defaultSort):Option[Ordering[Path]])
  order.onChange(refresh)

  def refresh:Unit =
    directory.get.map( d => readDirectory(d) ).getOrElse( clearEntries() )

  private def readDirectory(path:Path):Unit =
    rows.clear()
    rows.append(
      path.readDir
        .map { files => 
          if ! path.isRoot then
            Path.of("..") :: files 
          else
            files
        }
        .map { files =>
          order.get.map( ord => 
            files.sorted(ord)
          ).getOrElse( files )
        }.getOrElse( List.empty )
    )

  private def clearEntries():Unit = ()

  keyStrokeMap.bind( KeyStroke.KeyEvent(KeyName.Enter,false,false,false), enter )
  private def enter():Unit = 
    selection.focusedItem.get.foreach { path => 
      if path.name == ".." then moveUp()
      else if path.isDirectory.getOrElse(false) then moveIn(path)
    }

  private def moveUp():Unit = 
    directory.get.flatMap { path =>
      if conf.moveParentNormalizePath 
      then path.toAbsolutePath().normalize().parent
      else path.parent
    }.foreach(moveIn)
  
  private def moveIn(path:Path):Unit = 
    directory.set( Some(path) )
    if conf.clearSelectionOnCD then
      selection.indexes.clear()
    if conf.forceFirstRowFocused then
      selection.focusedIndex.set(Some(0))
  