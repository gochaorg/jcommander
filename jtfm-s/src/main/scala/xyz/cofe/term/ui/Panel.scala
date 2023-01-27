package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.ui.paint._

class Panel extends Widget 
  with WidgetChildren[Widget] 
  with LocationRWProp
  with SizeRWProp
  with FillBackground
  with PaintChildren
  with WidgetInput
