package xyz.cofe.jtfm

enum ObserverCollEvent[K,V]:
  case Insert(idx:K, a:V) extends ObserverCollEvent[K,V]
  case Delete(idx:K, a:V) extends ObserverCollEvent[K,V]
  case Update(idx:K, a:V, b:V) extends ObserverCollEvent[K,V]


