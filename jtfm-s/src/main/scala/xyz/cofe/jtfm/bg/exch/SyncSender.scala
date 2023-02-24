package xyz.cofe.jtfm.bg.exch

import java.time.Duration
import java.time.Instant

class SyncSender[E,R,ID]( 
  output:Queue[(ID,E)], 
  input:Queue[(ID,R)],
)(using 
  generateId: IdGenerator[ID], 
  nonRelativeResponses: NonRelativeResponse[ID,R],
  conf: SyncSenderConf
):  
  def apply(event:E):Option[R] =
    val id = generateId.generate
    output.put((id,event))
    val timeoutMs = conf.timeout.abs().toMillis()
    val stopAfter = System.currentTimeMillis() + timeoutMs
    var result : Option[R] = None
    while result==None && (System.currentTimeMillis() < stopAfter) do
      val timeoutLeft = Duration.ofMillis(stopAfter - System.currentTimeMillis())
      input.read(timeoutLeft, conf.throttle) match
        case None => 
        case Some((resId,response)) =>
          if resId==id 
          then result = Some(response)
          else nonRelativeResponses.accept(resId, response)
    result

