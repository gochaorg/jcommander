package xyz.cofe.jtfm.bg

class AsnycSender[E,R,ID]( 
  output:Queue[(ID,E)], 
  input:Queue[(ID,R)],  
)(using 
  generateId: IdGenerator[ID], 
  nonRelativeResponses: NonRelativeResponse[ID,R]
):
  @volatile private var waits:Map[ID,R=>Unit] = Map.empty

  def poll:Unit =
    input.poll match
      case Some((id,ev)) =>
        val call = this.synchronized {
          waits.get(id) match
            case Some(reciver) =>
              waits = waits.removed(id)
              () => reciver(ev)
            case None => 
              () => nonRelativeResponses.accept(id,ev)
        }
        call()
      case None =>

  def apply(event:E)(reciever:R=>Unit):Unit =
    this.synchronized {
      val id = generateId.generate
      waits = waits + ( id -> reciever )
      output.put((id,event))
    }

class AsyncReciver[E,R,ID](
  input:Queue[(ID,E)],
  output:Queue[(ID,R)],
  consumer:(E,R=>Unit)=>Unit
):
  def poll:Unit =
    input.poll match
      case Some((id,ev)) =>
        consumer(ev, r => {
          output.put(id,r)
        })
      case None => 
    