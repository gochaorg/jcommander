package xyz.cofe.jtfm.ui.table

import xyz.cofe.term.ui.Button
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Color
import java.nio.file.Path
import xyz.cofe.term.common.Size
import xyz.cofe.jtfm.ui.cd.ChangeDirDialog

trait DirectoryTableLabel extends DirectoryTableBase:
  val directoryButton = Button()
  children.append(directoryButton)
  directoryButton.location = Position(1,0)
  directoryButton.foregroundColor = Color.Black
  directoryButton.backgroundColor = Color.White
  directoryButton.focus.acceptFocusOnMouseEvent = false

  directoryButton.action {
    ChangeDirDialog.open( directory.get ).ok.listen { chDir =>
      directory.set(Some(chDir))
      selection.focusedIndex.set(Some(0))
      focus.request
    }
  }

  private def computeHomeRelative(homeAbs:Path, dirAbs:Path):String =
    if homeAbs.toString() == dirAbs.toString() 
    then "~"
    else
      if dirAbs.toString().startsWith(homeAbs.toString())
      then Path.of("~").resolve( homeAbs.relativize(dirAbs) ).toString()
      else dirAbs.toAbsolutePath().toString()

  private def refreshDirButton(dirOpt:Option[Path]):Unit =
    dirOpt match
      case None => directoryButton.visible = false
      case Some(value) =>
        directoryButton.visible = true

        val home = Path.of(System.getProperty("user.home")).toAbsolutePath().normalize()
        val dir = value.toAbsolutePath().normalize()
        val str = computeHomeRelative(home,dir)

        val maxWidth = size.get.width()-2
        val renderStr = if str.length() <= maxWidth then str else str.drop( str.length() - maxWidth )

        directoryButton.text.set(renderStr)
        directoryButton.size.set(Size(renderStr.length(), 1))

  directory.onChange( (_,dirOpt) => refreshDirButton(dirOpt) )
  size.onChange { refreshDirButton(directory.get) }
  refreshDirButton( directory.get )

