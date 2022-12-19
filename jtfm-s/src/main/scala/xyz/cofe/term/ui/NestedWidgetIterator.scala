package xyz.cofe.term.ui

class NestedWidgetIterator(roots:Seq[Widget]) extends Iterator[List[Widget]]:
  var workSet: List[List[Widget]] = roots.toList.map { root => List(root) }

  def hasNext: Boolean = workSet.nonEmpty

  def next(): List[Widget] = 
    val res = workSet.head
    workSet = workSet.tail
    res.last match
      case childProp: WidgetChildren[?] =>
        val children : List[Widget] = childProp.children.get
        workSet = children.map { cw => res :+ cw } ++ workSet
      case _ => 
    res