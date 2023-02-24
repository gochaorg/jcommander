package xyz.cofe.jtfm.bg.copy

trait Nested[A]:
  def hasNested(a:A):Boolean
  def nestedOf(a:A):List[A]