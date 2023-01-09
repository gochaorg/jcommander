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
  def walk = LikeTree.Walk(node)

trait TreePath[A:LikeTree]:
  def node:A
  def newChild(a:A):TreePath[A]
  def parent:Option[TreePath[A]]
  def listToLeaf:List[A]
  def root:A = listToLeaf.head
  def selfSibIndex:Option[Int] =
    parent.flatMap { prnt =>
      val i = prnt.node.nodes.indexOf(node) 
      if i>=0 then Some(i) else None
    }
  def leftSib:Option[TreePath[A]] =
    selfSibIndex.flatMap { selfIdx => 
      if selfIdx<=0 
      then None
      else 
        parent.flatMap { prnt =>
          val sibs = prnt.node.nodes
          if selfIdx >= sibs.size 
          then None
          else 
            val sib = sibs(selfIdx-1)
            Some(prnt.newChild(sib))
        }
    }
  def rightSib:Option[TreePath[A]] =
    selfSibIndex.flatMap { selfIdx => 
      if selfIdx<0 
      then None
      else 
        parent.flatMap { prnt =>
          val sibs = prnt.node.nodes
          if selfIdx >= (sibs.size-1)
          then None
          else 
            val sib = sibs(selfIdx+1)
            Some(prnt.newChild(sib))
        }
    }

  def firstLeftChild:Option[TreePath[A]] =
    val childs = node.nodes
    if childs.isEmpty
    then None
    else Some(newChild(childs.head))

  def righter:Option[TreePath[A]] =
    rightSib.orElse(
      parent.flatMap( prnt => prnt.righter )
    )

  def rightDeepest:Option[TreePath[A]] =
    val childs = node.nodes
    if childs.isEmpty
    then None
    else 
      val right = newChild( childs.last )
      Some( right.rightDeepest.getOrElse( right ) )

  def nextByDeep:Option[TreePath[A]] =
    firstLeftChild.orElse( righter )

  def lefter:Option[TreePath[A]] =
    leftSib.flatMap { lftSib =>
      lftSib.rightDeepest.orElse(Some(lftSib))
    }

  def prevByDeep:Option[TreePath[A]] =
    lefter.orElse(parent)

case class RTreePath[A:LikeTree]( rpath:List[A] ) extends TreePath[A]:
  def node:A = rpath.last
  def newChild(a:A):RTreePath[A] = RTreePath( rpath :+ a )
  def parent:Option[RTreePath[A]] =
    if rpath.size>1 then
      Some(RTreePath(rpath.take(rpath.size-1)))
    else
      None
  def listToLeaf:List[A] = rpath

object LikeTree:
  class Walk[T:LikeTree](node:T):
    def path = TreeNodeIter(List(node))

  class TreeNodeIter[T:LikeTree]( roots:List[T] ) extends Iterator[ RTreePath[T] ]:
    var workSet:List[RTreePath[T]] = roots.map { root => RTreePath(List(root)) }
    def hasNext: Boolean = workSet.nonEmpty
    def next(): RTreePath[T] = 
      val res = workSet.head
      val next = res.node.nodes.map { n => res.newChild(n) }
      workSet = next ++ workSet.tail
      res
