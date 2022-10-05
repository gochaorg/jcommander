package xyz.cofe.jtfm

enum ObserverCollEvent[A]:
  case Insert(idx:Int, a:A) extends ObserverCollEvent[A]
  case Delete(idx:Int, a:A) extends ObserverCollEvent[A]
  case Update(idx:Int, a:A, b:A) extends ObserverCollEvent[A]


