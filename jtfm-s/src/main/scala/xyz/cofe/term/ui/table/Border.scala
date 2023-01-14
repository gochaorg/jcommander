package xyz.cofe.term.ui.table

import xyz.cofe.lazyp.Prop

case class Border(
  left: Delimeter,
  top: Delimeter,
  right: Delimeter,
  bottom: Delimeter
)

trait BorderProp:
  val border = Prop.rw(
    Border(
      left   = Delimeter.SingleLine,
      right  = Delimeter.SingleLine,
      top    = Delimeter.SingleLine,
      bottom = Delimeter.SingleLine
    )
  )