package xyz.cofe.jtfm.bg.copy

import scala.annotation.tailrec

class CopyRecursive[What,S](
  isNested:(What,S)=>Boolean,
  nestedOf:(What,S)=>List[What],
  copyLeaf:(What,S)=>Option[S],
  createDir:(What,S)=>Option[S],
):
  def copy(what:What, state:S):Option[S] =
    copy(List(what),state)

  @tailrec
  private def copy(ws:List[What],state:S):Option[S] =
    if ws.isEmpty then Some(state)
    else
      val v = ws.head
      if isNested(v,state)
      then 
        val ws1 = nestedOf(v,state) ++ ws.tail
        createDir(v,state) match
          case None => None
          case Some(s) => copy(ws1,s)
      else
        val ws1 = ws.tail
        copyLeaf(v,state) match
          case None => None
          case Some(s) => copy(ws1, s)
        