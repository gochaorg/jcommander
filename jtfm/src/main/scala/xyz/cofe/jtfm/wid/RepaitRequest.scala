package xyz.cofe.jtfm.wid

trait RepaitRequest[SELF] {
  def repaitRequest( self:SELF ):Unit
}

object RepaitRequest {
  def dummy[S]:RepaitRequest[S] = new RepaitRequest[S] {
    override def repaitRequest(self: S): Unit = ()
  }
  
  implicit def currentCycle[S]: RepaitRequest[S] = new RepaitRequest[S] {
    import wc.State
    override def repaitRequest(self: S): Unit = {
      WidgetCycle.tryGet match {
        case Some(wc) =>
          val rq: ()=>Unit = ()=>{
            wc.state match {
              case x:State.Work =>
                val y: State.Work= x.asInstanceOf[State.Work]
                y.renderTree.repaitRequest()
                ()
              case _ => ()
            }
          }

          wc.state match {
            case s: State.Init => s.asInstanceOf[State.Init].jobs.add( rq )
            case s: xyz.cofe.jtfm.wid.wc.State.Work =>
              s.asInstanceOf[State.Work].jobs.add( rq )
            case _: xyz.cofe.jtfm.wid.wc.State.End => ()
          }
        case _ => ()
      }
    }
  }
}