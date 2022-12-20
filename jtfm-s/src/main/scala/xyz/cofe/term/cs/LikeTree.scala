package xyz.cofe.term.cs

trait LikeTree[N]:
  def node(index:Int):Option[N]
  def nodesCount:Int