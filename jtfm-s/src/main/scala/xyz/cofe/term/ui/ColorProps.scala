package xyz.cofe.term.ui

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Color
import xyz.cofe.lazyp.ReadWriteProp

implicit def colorProp2Color( prop:Prop[Color] ):Color = prop.get

trait BackgroundColor:
  val backgroundColor: ReadWriteProp[Color] = ReadWriteProp(Color.Black)  
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    backgroundColor.onChange( wid.repaint )
  def backgroundColor_=( col:Color ):Unit = backgroundColor.set(col)

trait ForegroundColor:
  val foregroundColor: ReadWriteProp[Color] = ReadWriteProp(Color.White)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    foregroundColor.onChange( wid.repaint )
  def foregroundColor_=( col:Color ):Unit = foregroundColor.set(col)

trait FocusOwnerBgColor:
  val focusOwnerBgColor: ReadWriteProp[Color] = ReadWriteProp(Color.BlackBright)  
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusOwnerBgColor.onChange( wid.repaint )
  def focusOwnerBgColor_=( col:Color ):Unit = focusOwnerBgColor.set(col)

trait FocusOwnerFgColor:
  val focusOwnerFgColor: ReadWriteProp[Color] = ReadWriteProp(Color.YellowBright)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusOwnerFgColor.onChange( wid.repaint )
  def focusOwnerFgColor_=( col:Color ):Unit = focusOwnerFgColor.set(col)

trait FocusContainerBgColor:
  val focusContainerBgColor: ReadWriteProp[Color] = ReadWriteProp(Color.BlackBright)  
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusContainerBgColor.onChange( wid.repaint )
  def focusContainerBgColor_=( col:Color ):Unit = focusContainerBgColor.set(col)

trait FocusContainerFgColor:
  val focusContainerFgColor: ReadWriteProp[Color] = ReadWriteProp(Color.WhiteBright)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusContainerFgColor.onChange( wid.repaint )
  def focusContainerFgColor_=( col:Color ):Unit = focusContainerFgColor.set(col)
