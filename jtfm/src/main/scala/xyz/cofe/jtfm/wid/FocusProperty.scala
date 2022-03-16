package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.ev.{EvalProperty, Property}

trait FocusProperty[SELF : RepaitRequest](repait:Boolean=false) {
  self: Widget[_] =>
  
  class FocusProp extends EvalProperty[Boolean, SELF](
    compute = ()=>{
      WidgetCycle.tryGet.flatMap( _.workState ).flatMap( _.inputProcess.focusOwner.map( w => w==self ) ).getOrElse( false )
    },
    initial = Some(false)
  ) {
  
    def onGain( from:Option[Widget[_]] ):Unit = recompute()
    def onLost( newOwner:Option[Widget[_]] ):Unit = recompute()
    
    def contains:Boolean = {
      WidgetCycle.tryGet.flatMap( _.workState ).flatMap( _.inputProcess.focusOwner ).map( _.widgetPath ).contains(self)
    }
    
    def request():Unit = {
      WidgetCycle.tryGet.flatMap( _.workState ).map( _.inputProcess ).foreach( inp => {
        inp.focusOwner match {
          case Some(fo) =>
            if ( fo!=self ) {
              inp.focusRequest(self)
            }
          case _ =>
            inp.focusRequest(self)
        }
      })
    }
  }
  
  lazy val focus : FocusProp = {
    if (repait) {
      val prop = FocusProp()
      prop.observe( (prop,old,cur)=>{
        val rep = implicitly[RepaitRequest[SELF]]
        rep.repaitRequest(self.asInstanceOf[SELF])
      })
      prop
    } else {
      FocusProp()
    }
  }
}
