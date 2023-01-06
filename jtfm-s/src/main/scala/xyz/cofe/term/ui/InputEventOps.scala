package xyz.cofe.term.ui

import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.InputCharEvent

extension (inputEvent:InputEvent)
  def isModifiersDown:Boolean =
    inputEvent match
      case ke:InputKeyEvent  => ke.isAltDown() || ke.isControlDown() || ke.isShiftDown()
      case ce:InputCharEvent => ce.isAltDown() || ce.isControlDown() || ce.isShiftDown()
      case _ => false
  def isModifiers(altDown:Boolean, controlDown:Boolean, shiftDown:Boolean):Boolean =
    inputEvent match
      case ke:InputKeyEvent  => ke.isAltDown()==altDown && ke.isControlDown()==controlDown && ke.isShiftDown()==shiftDown
      case ce:InputCharEvent => ce.isAltDown()==altDown && ce.isControlDown()==controlDown && ce.isShiftDown()==shiftDown
      case _ => false
    
