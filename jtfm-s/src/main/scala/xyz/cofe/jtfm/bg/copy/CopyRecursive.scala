package xyz.cofe.jtfm.bg.copy

import scala.annotation.tailrec
import java.util.concurrent.atomic.AtomicBoolean

class CopyRecursive[What: Nested,S](
  copyLeaf:  (What,S)=>Either[S,S],
  createDir: (What,S)=>Either[S,S],  
)(using cancelSignal:CancelSignal):
  val stopFlag = new AtomicBoolean(false)
  cancelSignal.listen { stopFlag.set(true) }

  def copy(what:What, state:S):Either[S,S] =
    copy(List(what),state,stopFlag)

  @tailrec
  private def copy(ws:List[What],state:S,stopFlag:AtomicBoolean):Either[S,S] =
    if stopFlag.get()
    then Left(state)
    else
      val nestedItf = summon[Nested[What]]
      if ws.isEmpty then Right(state)
      else
        val v = ws.head
        if nestedItf.hasNested(v)
        then 
          val ws1 = nestedItf.nestedOf(v) ++ ws.tail
          createDir(v,state) match
            case Left(e) => Left(e)
            case Right(s) => copy(ws1,s,stopFlag)
        else
          val ws1 = ws.tail
          copyLeaf(v,state) match
            case Left(e) => Left(e)
            case Right(s) => copy(ws1,s,stopFlag)
        