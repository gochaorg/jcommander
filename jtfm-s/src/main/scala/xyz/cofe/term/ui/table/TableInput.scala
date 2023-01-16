package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.WidgetInput
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputMouseButtonEvent

import TableGridProp.ContentBlock.HeaderBlock
import xyz.cofe.term.common.MouseButton
import xyz.cofe.term.ui.isModifiersDown

trait TableInput[A]
extends WidgetInput
with TableRowsProp[A]
with TableGridPaint[A]
:
  override def input(inputEvent: InputEvent): Boolean = 
    inputEvent match
      case me:InputMouseButtonEvent => processInput(me)
      case _ => false

  protected def processInput(me:InputMouseButtonEvent):Boolean =
    val matchedHeadBlock = headersBlocks.get.map { block =>
      (block.rect.contains(me.position()), block)
    }.filter((matched,_)=>matched)
     .map((_,headBlock)=>headBlock)
     .headOption.map(b => processHeaderInput(me,b))

    val matchedDataBlock = dataBlocks.get.map { dataBlock => 
      (dataBlock.rect.contains(me.position()), dataBlock)
    }.filter((matched,_)=>matched)
     .map((_,dataBlock)=>dataBlock)
     .headOption
     .flatMap { dataBlock => 
       val rowVisibleOffset = me.position.y - dataBlock.rect.top
       val rowIndex = scroll.value.get + rowVisibleOffset
       rows.getAt(rowIndex).map { dataRow => 
        processCellInput(me,dataRow,dataBlock.col,rowIndex) 
      }
     }

    matchedHeadBlock.orElse(matchedDataBlock).getOrElse(processDefaultInput(me))

  protected def processHeaderInput(me:InputMouseButtonEvent, block:HeaderBlock[A]):Boolean =
    println(s"header ${block.col.id} ${block.col.title.get} / ${me.button()} ${me.pressed()}")
    if me.button()==MouseButton.Left && me.pressed() && !me.isModifiersDown 
    then true
    else false

  protected def processCellInput(me:InputMouseButtonEvent, dataRow:A, column:Column[A,_], rowIndex:Int):Boolean =
    println(s"cell ${dataRow} ${column.id} ${rowIndex} / ${me.button()} ${me.pressed()}")
    if me.button()==MouseButton.Left && me.pressed() && !me.isModifiersDown 
    then true
    else false
    
  protected def processDefaultInput(me:InputMouseButtonEvent):Boolean =
    if me.button()==MouseButton.Left && me.pressed() && !me.isModifiersDown 
    then true
    else false