package xyz.cofe.term.ui.ses

import xyz.cofe.term.ui.Widget
import scala.reflect.ClassTag
import xyz.cofe.term.ui.VisibleProp

case class Navigator[W <: Widget : ClassTag]( from:Widget, move:Widget=>Option[Widget], skipFirst:Boolean=true, filter:W=>Boolean=(w:W)=>true ) extends Iterator[W]:
  def fetch( from:Widget ):Option[W] =
    var cur = from
    var stop = false
    val trgtCls = summon[ClassTag[W]].runtimeClass
    var res : Option[W] = None
    var cycle = 0
    def fetchNext = {
      move(cur) match
        case None => 
          stop = true
          res = None
        case Some(next) =>
          cur = next
    }
    while !stop do
      cycle += 1        
      if trgtCls.isAssignableFrom( cur.getClass() ) && filter(cur.asInstanceOf[W])
      then 
        res = Some(cur.asInstanceOf[W])
        if cycle==1 
        then 
          if skipFirst 
          then 
            res = None
            fetchNext 
          else 
            stop = true
        else stop = true
      else
        fetchNext
    res

  var current = fetch(from)
  
  def hasNext: Boolean = current.isDefined
  def next(): W = 
    val res = current.get
    current = fetch(res)
    res

  def visibleOnly:Navigator[W] = copy(
    filter = wid => wid match
      case vp:VisibleProp => vp.visible.inTree
      case _ => false      
  )

  def typed[W <: Widget:ClassTag]:Navigator[W] = Navigator[W](from, move, skipFirst, (w:W)=>true )

