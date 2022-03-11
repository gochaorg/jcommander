package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.ev.OwnProperty

trait OpaqueProperty[SELF : RepaitRequest] {
  self: Widget[_] =>
  
  lazy val opaque:OwnProperty[Boolean,SELF] =
    OwnProperty(true,self.asInstanceOf[SELF])
      .observe( (prop,old,cur)=>{
        val rep = implicitly[RepaitRequest[SELF]]
        rep.repaitRequest(prop.owner)
      })
      ._1
}
