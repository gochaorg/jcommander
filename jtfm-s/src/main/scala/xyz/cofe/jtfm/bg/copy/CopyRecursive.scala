package xyz.cofe.jtfm.bg.copy

import scala.annotation.tailrec

class CopyRecursive[What: Nested,S](
  copyLeaf:(What,S)=>Option[S],
  createDir:(What,S)=>Option[S],
):
  def copy(what:What, state:S):Option[S] =
    copy(List(what),state)

  @tailrec
  private def copy(ws:List[What],state:S):Option[S] =
    val nestedItf = summon[Nested[What]]
    if ws.isEmpty then Some(state)
    else
      val v = ws.head
      if nestedItf.hasNested(v)
      then 
        val ws1 = nestedItf.nestedOf(v) ++ ws.tail
        createDir(v,state) match
          case None => None
          case Some(s) => copy(ws1,s)
      else
        val ws1 = ws.tail
        copyLeaf(v,state) match
          case None => None
          case Some(s) => copy(ws1, s)
        