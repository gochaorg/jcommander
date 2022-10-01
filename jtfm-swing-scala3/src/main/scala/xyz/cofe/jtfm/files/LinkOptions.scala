package xyz.cofe.jtfm.files

import java.nio.file.LinkOption

trait LinkOptions:
  def options:Seq[LinkOption]

object LinkOptions:
  given LinkOptions with
    def options:Seq[LinkOption] = List()
