package xyz.cofe.term.geom

import Symbols.StyledSide
import xyz.cofe.term.common.Position

/** 
 * Пересечение с линией
 */
case class Intersection( p:Position, side:List[StyledSide]=List() )
