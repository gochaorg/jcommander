package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.ev.OwnProperty

/**
 * Свойство - Видимость
 */
trait VisibleProperty[SELF : RepaitRequest] {
  self: Widget[_] =>

  class VisibleProp[SELF:RepaitRequest]( private val init:Boolean ) extends OwnProperty[Boolean,SELF](init, self.asInstanceOf[SELF]) {
    def inRoot:Boolean = {
      widgetPath.map(_.visible.value).foldLeft(true)( (a,b) => a && b )
    }
  }
  
  lazy val visible:VisibleProp[SELF] =
    VisibleProp[SELF](true)
      .observe( (prop,old,cur)=>{
        val rep = implicitly[RepaitRequest[SELF]]
        rep.repaitRequest(prop.owner)
      })
      ._1.asInstanceOf[VisibleProp[SELF]]
}
