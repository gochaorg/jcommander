package xyz.cofe.jtfm.bg.copy

class CopyRecursive[What,S](
  isNested:(What,S)=>Boolean,
  nestedOf:(What,S)=>List[What],
  copyLeaf:(What,S)=>Option[S],
  createDir:(What,S)=>Option[S],
):
  def copy(what:What, state:S):Option[S] =
    if ! isNested(what,state) 
    then Some(state)
    else
      createDir(what,state) match
        case None => None
        case Some(state) =>
          val nestedList = nestedOf(what,state)
          if nestedList.isEmpty 
          then Some(state)
          else
            Iterator.iterate( (nestedList, Option(state)) ){ 
              case s @ (list,Some(state)) =>
                if list.isEmpty 
                then s
                else ( list.tail, {
                  val ns = {
                    if isNested(list.head,state)
                    then copy(list.head,state)
                    else copyLeaf(list.head,state)
                  }
                  println(s"ns $ns")
                  ns
                })
              case s @ (list,None) => s
            }.takeWhile { case(list, state) =>
              list.nonEmpty && state.isDefined
            }.toList.lastOption.flatMap( _._2 )
