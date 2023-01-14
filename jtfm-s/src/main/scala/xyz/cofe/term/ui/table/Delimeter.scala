package xyz.cofe.term.ui.table

enum Delimeter:
  case None
  case Space(width:Int)
  case SingleLine
  case DoubleLine

  def size = this match
    case None => 0
    case Space(width) => width
    case SingleLine => 1
    case DoubleLine => 1
  

