package xyz.cofe.jtfm.store.json

extension[T] (optValue:Option[T])
  def lift:Either[String,T] = optValue match
    case Some(v) => Right(v)
    case None => Left("value not exists")
  def lift(errMessage:String):Either[String,T] = optValue match
    case Some(v) => Right(v)
    case None => Left(errMessage)

