package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.TextColor
import xyz.cofe.jtfm.ev.OwnProperty

/** Операции на цветом */
object ColorOps {
  implicit class ColorPropExt[S]( colorProp: OwnProperty[TextColor,S] ) {
    def value_= ( color: Color ):Unit = {
      colorProp.value = color.textColor
    }
  }
}

implicit def colorProp2Color[S]( colorProp: OwnProperty[TextColor,S] ):TextColor =
  colorProp.value