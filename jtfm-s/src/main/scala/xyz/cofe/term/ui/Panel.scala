package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.paint.PaintCtx

class Panel extends Widget 
  with WidgetChildren[Widget] 
  with VisibleProp
  with LocationRWProp
  with SizeRWProp
  with FillBackground
  with PaintChildren
  with WidgetInput
