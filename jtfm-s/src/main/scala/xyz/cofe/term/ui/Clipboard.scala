package xyz.cofe.term.ui

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

//trait Clipboard

object ClipboardAWT:
  val clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()

  def readString:Option[String] =
    val transfer = clipboard.getContents(null)
    if transfer.isDataFlavorSupported(DataFlavor.stringFlavor) then
      val str = transfer.getTransferData(DataFlavor.stringFlavor).asInstanceOf[String]
      Some(str)
    else
      None

  def writeString(str:String):Unit =
    val strSel = new StringSelection(str)
    clipboard.setContents(strSel,strSel)