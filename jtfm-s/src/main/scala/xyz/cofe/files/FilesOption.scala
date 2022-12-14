package xyz.cofe.files

import java.nio.file.LinkOption
import java.nio.file.attribute.FileAttribute
import java.nio.file.CopyOption
import java.nio.file.OpenOption

trait FilesOption:
  def copy:FilesOption.Opts = FilesOption.Opts(
    linkOptions = linkOptions,
    fileAttributes = Seq(),
    copyOptions = copyOptions,
    openOptions = openOptions
  )
  def linkOptions:Seq[LinkOption]
  def fileAttributes:Seq[FileAttribute[_]]
  def copyOptions:Seq[CopyOption]
  def openOptions:Seq[OpenOption]
  
object FilesOption:
  given defaultOption:FilesOption = new FilesOption {
    def linkOptions: Seq[LinkOption] = Seq()
    def fileAttributes: Seq[FileAttribute[_]] = Seq()
    def copyOptions: Seq[CopyOption] = Seq()
    def openOptions: Seq[OpenOption] = Seq()
  }

  case class Opts(
    linkOptions:Seq[LinkOption],
    fileAttributes:Seq[FileAttribute[_]],
    copyOptions:Seq[CopyOption],
    openOptions:Seq[OpenOption]
  ) extends FilesOption