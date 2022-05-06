package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.ev.OwnProperty

trait TextProperty[SELF : RepaitRequest] {
  self: Widget[_] =>
  
  lazy val text:OwnProperty[String,SELF] =
    OwnProperty("",self.asInstanceOf[SELF])
      .observe( (prop,old,cur)=>{
        val rep = implicitly[RepaitRequest[SELF]]
        rep.repaitRequest(prop.owner)
      })
      ._1
}
