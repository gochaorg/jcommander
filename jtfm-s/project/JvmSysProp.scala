package dist

sealed trait JvmSysProp {
  def cmdLine:String
}
object JvmSysProp {
  case class Custom(cmdLine:String) extends JvmSysProp
}

