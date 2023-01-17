package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.WidgetInput
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputMouseButtonEvent

import TableGridProp.ContentBlock.HeaderBlock
import xyz.cofe.term.common.MouseButton
import xyz.cofe.term.ui.isModifiersDown
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.ui.KeyStroke
import xyz.cofe.lazyp.Prop
import xyz.cofe.term.ui.KeyStrokeMap
import xyz.cofe.term.common.InputCharEvent
import xyz.cofe.term.common.InputKeyboardEvent

trait TableInput[A]
extends WidgetInput
with TableRowsProp[A]
with TableGridPaint[A]
:
  override def input(inputEvent: InputEvent): Boolean = 
    inputEvent match
      case me:InputMouseButtonEvent => processMouseButtonInput(me)
      case ke:InputKeyEvent  => processKeyboardInput(ke)
      case ce:InputCharEvent => processKeyboardInput(ce)
      case _ => false

  protected def processMouseButtonInput(me:InputMouseButtonEvent):Boolean =
    val matchedHeadBlock = headersBlocks.get.map { block =>
      (block.rect.contains(me.position()), block)
    }.filter((matched,_)=>matched)
     .map((_,headBlock)=>headBlock)
     .headOption.map(b => processHeaderMouseInput(me,b))

    val matchedDataBlock = dataBlocks.get.map { dataBlock => 
      (dataBlock.rect.contains(me.position()), dataBlock)
    }.filter((matched,_)=>matched)
     .map((_,dataBlock)=>dataBlock)
     .headOption
     .flatMap { dataBlock => 
       val rowVisibleOffset = me.position.y - dataBlock.rect.top
       val rowIndex = scroll.value.get + rowVisibleOffset
       rows.getAt(rowIndex).map { dataRow => 
        processCellMouseInput(me,dataRow,dataBlock.col,rowIndex) 
      }
     }

    matchedHeadBlock.orElse(matchedDataBlock).getOrElse(processDefaultMouseInput(me))

  protected def processHeaderMouseInput(me:InputMouseButtonEvent, block:HeaderBlock[A]):Boolean =
    if me.button()==MouseButton.Left && me.pressed() && !me.isModifiersDown 
    then true
    else false

  protected def processCellMouseInput(me:InputMouseButtonEvent, dataRow:A, column:Column[A,_], rowIndex:Int):Boolean =
    if me.button()==MouseButton.Left && me.pressed() && !me.isModifiersDown 
    then 
      selection.set(rowIndex)
      selection.focusedIndex.set(Some(rowIndex))
      true
    else false
    
  protected def processDefaultMouseInput(me:InputMouseButtonEvent):Boolean =
    if me.button()==MouseButton.Left && me.pressed() && !me.isModifiersDown 
    then true
    else false

  
  lazy val keyStrokeMap = KeyStrokeMap[()=>Unit](predefKeyStrokes)
  private lazy val keyStrokeParser = KeyStrokeMap.KeyStrokeInputParser[()=>Unit](keyStrokeMap)

  protected def processKeyboardInput(ke:InputKeyboardEvent):Boolean =
    var matched = false
    keyStrokeParser.input(ke){ action => 
      matched = true
      action() 
    }
    matched

  private lazy val predefKeyStrokes : Map[KeyStroke,Set[()=>Unit]] = Map(
    KeyStroke.KeyEvent(KeyName.Down,     false,false,false) -> Set(this.moveDown),
    KeyStroke.KeyEvent(KeyName.Up,       false,false,false) -> Set(this.moveUp),
    KeyStroke.KeyEvent(KeyName.PageDown, false,false,false) -> Set(this.movePageDown),
    KeyStroke.KeyEvent(KeyName.PageUp,   false,false,false) -> Set(this.movePageUp),
  )

  def moveDown():Unit =
    println("moveDown")
    selection.focusedIndex.get match
      case None => 
        renderDataRows.get.headOption.foreach { dataRow => 
          selection.focusedIndex.set(Some(dataRow.index))
          selection.set(dataRow.index)
        }
      case Some(focusedIndex) =>        

  def moveUp():Unit =
    println("moveUp")

  def movePageDown():Unit =
    println("movePageDown")

  def movePageUp():Unit =
    println("movePageUp")
