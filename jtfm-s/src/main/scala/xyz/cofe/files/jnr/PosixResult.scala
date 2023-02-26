package xyz.cofe.files.jnr

case class PosixResult[R]( 
  private val result:R, 
  errors:List[PosixError], 
  output:Option[String], 
  errput:Option[String] 
):
  def toOption:Option[R] =
    if errors.isEmpty then Some(result) else None

  def toEither:Either[PosixResult.Errors,R] =
    if errors.isEmpty then Right(result) else Left(PosixResult.Errors(errors,output,errput))

  def getOrElse(defaultValue: => R):R =
    toOption.getOrElse(defaultValue)

  def map[R1](f:R=>R1):PosixResult[R1] =
    if errors.isEmpty then PosixResult(f(result),errors,output,errput) else this.asInstanceOf[PosixResult[R1]]

  def flatMap[R1](f:R=>PosixResult[R1]):PosixResult[R1] =
    if errors.isEmpty then f(result) else this.asInstanceOf[PosixResult[R1]]


object PosixResult:
  def pure[R]( r:R ):PosixResult[R] = PosixResult(r,List.empty,None,None)
  case class Errors(errors:List[PosixError], output:Option[String], errput:Option[String])

