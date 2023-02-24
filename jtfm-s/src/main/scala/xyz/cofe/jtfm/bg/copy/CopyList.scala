package xyz.cofe.jtfm.bg.copy

class CopyList[What,S](
  copyItem:(What,S)=>Option[S]
):
  def copy( fromTo:List[What], state:S ):Option[S] =
    if fromTo.isEmpty then
      Some(state)
    else
      Iterator.iterate( (fromTo.head, Option(state), fromTo.tail) ) {
        case (what,Some(state),tail) =>
          val s = copyItem(what,state)
          val next = tail
            .headOption.map { case nextWhat => (nextWhat,s,tail.tail) }
            .getOrElse( (what,None,tail) )
          next
        case s => s
      }.takeWhile { case (_, copt, _) => copt.isDefined }.toList.lastOption.flatMap( _._2 )

