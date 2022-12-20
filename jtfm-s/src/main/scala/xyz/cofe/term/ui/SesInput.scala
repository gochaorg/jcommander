package xyz.cofe.term.ui

import xyz.cofe.term.common.InputResizeEvent

trait SesInput extends SesPaint:
  

  protected  def processInput():Unit =
    val inputEvOpt = console.read()
    if( inputEvOpt.isPresent() ){
      val inputEv = inputEvOpt.get()
      inputEv match
        case resizeEv:InputResizeEvent =>
          val size = resizeEv.size()
          screenBuffer.resize(size)
          rootWidget.size.set(size)
        case _ => 
          rootWidget.children.nested.foreach { path => 
            path.last match
              case wInput:WidgetInput =>
                wInput.input(inputEv)
              case _ =>
          }
    }

