package xyz.cofe.jtfm.ui.table

import xyz.cofe.jtfm.ui.table.DirectoryTableBase
import xyz.cofe.term.ui.Button
import xyz.cofe.term.common.Position
import xyz.cofe.files._
import xyz.cofe.files.size
import xyz.cofe.term.common.Size
import xyz.cofe.log._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import conf._

trait DirectoryTableSelectLabel(using colors:DirectoryTableColorConf) extends DirectoryTableBase:
  private lazy implicit val logger : Logger = LoggerFactory.getLogger("xyz.cofe.jtfm.ui.table.DirectoryTableSelectLabel")

  val selectionButton:Button = Button()
  children.append(selectionButton)

  selectionButton.foregroundColor = colors.directoryLabel.foreground
  selectionButton.backgroundColor = colors.directoryLabel.background
  selectionButton.focus.acceptFocusOnMouseEvent = false

  size.onChange(renderSelection)
  selection.rows.onChange(renderSelection)

  private def renderSelection:Unit =
    val selectedFiles = selection.rows.get
    if selectedFiles.isEmpty then
      selectionButton.visible = false
    else
      selectionButton.visible = true
      selectionButton.location = Position(1,size.get.height()-1)
      val maxWidth = size.get.width()-2
      val sumSize = ByteSize(selectedFiles.flatMap { 
        file => file.isRegularFile.getOrElse(false) match
          case true  => List(file.size.getOrElse(0L))
          case false => List.empty
      }.sum)      
      val str = s"${selectedFiles.size} files / ${sumSize.precisionReadable}"
      val renderStr = str.take(maxWidth)
      selectionButton.text.set(renderStr)
      selectionButton.size.set(Size(renderStr.length(), 1))
