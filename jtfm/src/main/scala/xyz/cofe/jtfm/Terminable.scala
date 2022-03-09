package xyz.cofe.jtfm

trait Terminable[S] {
  def terminate(s:S):Unit
}
