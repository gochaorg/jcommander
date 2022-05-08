package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.wid.BackgroundProperty
import com.googlecode.lanterna.TextColor
import xyz.cofe.jtfm.gr.Rect

class ColorPanel 
  extends Widget[ColorPanel]
    with BackgroundProperty[ColorPanel]
{
  private val bgFgColors = List( 
    (TextColor.ANSI.BLACK, TextColor.ANSI.WHITE_BRIGHT),
    (TextColor.ANSI.RED, TextColor.ANSI.WHITE_BRIGHT),
    (TextColor.ANSI.GREEN, TextColor.ANSI.WHITE_BRIGHT),
    (TextColor.ANSI.YELLOW, TextColor.ANSI.WHITE_BRIGHT),
    (TextColor.ANSI.BLUE, TextColor.ANSI.WHITE_BRIGHT),
    (TextColor.ANSI.MAGENTA, TextColor.ANSI.WHITE_BRIGHT),
    (TextColor.ANSI.CYAN, TextColor.ANSI.WHITE_BRIGHT),
    (TextColor.ANSI.WHITE, TextColor.ANSI.WHITE_BRIGHT),

    (TextColor.ANSI.BLACK_BRIGHT, TextColor.ANSI.BLACK),
    (TextColor.ANSI.RED_BRIGHT, TextColor.ANSI.BLACK),
    (TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK),
    (TextColor.ANSI.YELLOW_BRIGHT, TextColor.ANSI.BLACK),
    (TextColor.ANSI.BLUE_BRIGHT, TextColor.ANSI.BLACK),
    (TextColor.ANSI.MAGENTA_BRIGHT, TextColor.ANSI.BLACK),
    (TextColor.ANSI.CYAN_BRIGHT, TextColor.ANSI.BLACK),
    (TextColor.ANSI.WHITE_BRIGHT, TextColor.ANSI.BLACK),
  )

  private var selectedColor:Option[TextColor] = None  
  def color:Option[TextColor] = selectedColor
  def color_=(clr:Option[TextColor]):Unit = {
    clr match {
      case None =>
        selectedColor = None
      case Some(col) =>
        labels.foreach { _._2.text.value = " " }
        labels.find{ case (bg,lbl) => bg.getRed==col.getRed && bg.getGreen==col.getGreen && bg.getBlue==col.getBlue } match {
          case Some( (col,lbl) ) =>
            lbl.text.value = "+"
          case _ =>
        }
    }
  }

  private val labels:List[(TextColor,Label)] = bgFgColors.zip( 0 until 100 ).map { case ((bg,fg),idx) =>
    val x = idx % 4
    val y = idx / 4
    val lbl = Label()
    lbl.text.value = " "
    lbl.foreground.value = fg
    lbl.background.value = bg
    lbl.rect.value = Rect(x,y).size(1,1)
    nested.append(lbl)
    lbl.onLeftClick {
      selectedColor = Some(bg)
      labels.foreach { _._2.text.value = " " }
      lbl.text.value = "+"
      true
    }
    (bg,lbl)
  }
}
