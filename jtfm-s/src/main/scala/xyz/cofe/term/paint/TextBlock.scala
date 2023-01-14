package xyz.cofe.term.paint

import xyz.cofe.term.buff.ScreenChar

case class TextBlock(
  lines: Seq[Seq[ScreenChar]]
):
  lazy val minMaxWidth:Option[(Int,Int)] = {
    lines.foldLeft( None:Option[(Int,Int)] ){ case (minMaxOpt,line) => 
      minMaxOpt.flatMap { case (minWi, maxWi) => 
        val len = line.size
        Some( len min minWi, len max maxWi )
      }.orElse(
        Some( (line.size, line.size) )
      )
    }
  }

  lazy val minWidth:Option[Int] = minMaxWidth.map(_._1)
  lazy val maxWidth:Option[Int] = minMaxWidth.map(_._2)


object TextBlock:
  def fill( width:Int, height:Int, chr:ScreenChar ) =
    new TextBlock(
      (0 until height).map { y => 
        (0 until width).map { x =>
          chr
        }.toSeq
      }.toSeq
    )