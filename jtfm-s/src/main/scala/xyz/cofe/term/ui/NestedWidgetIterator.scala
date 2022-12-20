package xyz.cofe.term.ui

import xyz.cofe.term.cs.ObserverList

class NestedWidgetIterator(roots:Seq[Widget]) extends Iterator[List[Widget]]:
  var workSet: List[List[Widget]] = roots.toList.map { root => List(root) }

  def hasNext: Boolean = workSet.nonEmpty

  def next(): List[Widget] = 
    val res = workSet.head
    workSet = workSet.tail
    res.last match
      case childProp: WidgetChildren[?] =>
        workSet = childProp.children.toList.map { cw => res :+ cw } ++ workSet
      case _ => 
    res

extension [C <: Widget](children:ObserverList[C])
  def nested = NestedWidgetIterator(children.toList)