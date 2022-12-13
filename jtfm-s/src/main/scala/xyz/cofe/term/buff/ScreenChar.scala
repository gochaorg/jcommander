package xyz.cofe.term.buff

import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Color

case class ScreenChar( char:Char, foreground:Color, background:Color )
case class PosScreenChar( char:ScreenChar, pos:Position )
