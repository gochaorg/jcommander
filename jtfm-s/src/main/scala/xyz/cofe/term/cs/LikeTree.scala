package xyz.cofe.term.cs

trait LikeTree[N]:
  def nodes(n:N):List[N]

extension [T:LikeTree](node:T)
  def node(index:Int):Option[T] = 
    val l = summon[LikeTree[T]].nodes(node)
    if index>=0 && index<l.size 
    then Some(l(index))
    else None

  def nodesCount():Int = summon[LikeTree[T]].nodes(node).size
  def nodes:List[T] = summon[LikeTree[T]].nodes(node)
  //def nodes

class TreeNodeIter[T:LikeTree]( roots:List[T] ) extends Iterator[ List[T] ]:
  var workSet:List[List[T]] = roots.map { root => List(root) }
  def hasNext: Boolean = workSet.nonEmpty
  def next(): List[T] = 
    val res = workSet.head
    workSet = res.nodes ++ workSet.tail
    res