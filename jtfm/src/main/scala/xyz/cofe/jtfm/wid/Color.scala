package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.TextColor

/** Предопределенные цвета */
enum Color( val textColor: TextColor ) {
  case Black extends Color( TextColor.ANSI.BLACK )
  case Red extends Color( TextColor.ANSI.RED )
  case Green extends Color( TextColor.ANSI.GREEN )
  case Yellow extends Color( TextColor.ANSI.YELLOW )
  case Blue extends Color( TextColor.ANSI.BLUE )
  case Magenta extends Color( TextColor.ANSI.MAGENTA )
  case Cyan extends Color( TextColor.ANSI.CYAN )
  case White extends Color( TextColor.ANSI.WHITE )
  case BlackBright extends Color( TextColor.ANSI.BLACK_BRIGHT )
  case RedBright extends Color( TextColor.ANSI.RED_BRIGHT )
  case GreenBright extends Color( TextColor.ANSI.GREEN_BRIGHT )
  case YellowBright extends Color( TextColor.ANSI.YELLOW_BRIGHT )
  case BlueBright extends Color( TextColor.ANSI.BLUE_BRIGHT )
  case MagentaBright extends Color( TextColor.ANSI.MAGENTA_BRIGHT )
  case CyanBright extends Color( TextColor.ANSI.CYAN_BRIGHT )
  case WhiteBright extends Color( TextColor.ANSI.WHITE_BRIGHT )
}
