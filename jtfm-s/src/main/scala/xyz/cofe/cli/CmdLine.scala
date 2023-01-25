package xyz.cofe.cli

object CmdLine:
  def apply[A:FromCmdLine](args:Seq[String]):Either[String,A] =
    FromCmdLine.parse[A](args.toList)

  def parse[A:FromCmdLine](args:Seq[String]):Either[String,(A,List[String])] =
    summon[FromCmdLine[A]].parse(args.toList)

  def default[A](fieldName:String, args:String*) = CmdLineDefault[A](fieldName, args:_*)
