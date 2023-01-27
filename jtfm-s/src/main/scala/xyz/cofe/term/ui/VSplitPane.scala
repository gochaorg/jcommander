package xyz.cofe.term.ui

import xyz.cofe.term.ui.prop.WidgetChildren
import xyz.cofe.term.ui.paint.PaintChildren
import xyz.cofe.term.ui.prop.LocationRWProp
import xyz.cofe.term.ui.prop.SizeRWProp
import xyz.cofe.lazyp.Prop
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.lazyp.ReleaseListener

//todo require ReadOnly children

class VSplitPane
extends WidgetInput
with LocationRWProp
with SizeRWProp
with WidgetChildren[Widget]
with PaintChildren
:
  children.onChange(recomputeDeferred)

  private var recomputedDeferred = false
  def recomputeDeferred:Unit = 
    recomputedDeferred = false
    Session.addJob {
      if ! recomputedDeferred then
        recompute
        recomputedDeferred = true
    }

  private var leftRightRelease:List[ReleaseListener] = List.empty
    
  def recompute:Unit = 
    ()
    // leftRightRelease.foreach(_())
    // leftRightRelease = List.empty

    // children.drop(2).filter { _.isInstanceOf[VisibleProp] }.map { _.asInstanceOf[VisibleProp] }.foreach { _.visible = false }
    
    // val leftRightList = children.take(2).toList
    // if leftRightList.size == 2 then

    //   // val leftWid  = leftRightList(0)
    //   // leftWid match
    //   //   case v:VisibleProp =>      

    //   val rightWid = leftRightList(1)
