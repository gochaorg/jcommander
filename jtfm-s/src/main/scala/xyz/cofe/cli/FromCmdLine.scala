package xyz.cofe.cli

import scala.deriving.*
import scala.compiletime.{erasedValue, summonInline, constValue}

trait CmdLineDefault[A]:
  def cmdLineDefault(fieldName:String):List[String]  

object CmdLineDefault:
  given [A]:CmdLineDefault[A] with
    def cmdLineDefault(fieldName:String):List[String] = List()

  case class DefValues[A](map:Map[String,List[String]]) extends CmdLineDefault[A]:
    def apply(fieldName:String, values:String*):DefValues[A] =
      copy(map = map + (fieldName->values.toList) )

    def cmdLineDefault(fieldName:String):List[String] =
      map.get(fieldName).getOrElse(List())

  def apply[A](fieldName:String, values:String*):DefValues[A]=
    DefValues(Map(fieldName->values.toList))

trait FromCmdLine[A]:
  def parse(args:List[String]):Either[String,(A,List[String])]

object FromCmdLine extends PredefCmdLineParsers:
  def parse[A:FromCmdLine](args:List[String]):Either[String,A] =
    summon[FromCmdLine[A]].parse(args).map { case(res,_) => res }

  ///////////////////////////////////////////////////////////////////////
  inline def summonAll[T <: Tuple]:List[FromCmdLine[_]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts) => summonInline[FromCmdLine[t]] :: summonAll[ts]

  inline def labelFromMirror[A](using m:Mirror.Of[A]):String = constValue[m.MirroredLabel]

  inline def labelsFrom[A <: Tuple]:List[String] = inline erasedValue[A] match
    case _:EmptyTuple => Nil
    case _:(head *: tail) => 
      constValue[head].toString() :: labelsFrom[tail]

  inline def labelsOf[A](using m:Mirror.Of[A]) = labelsFrom[m.MirroredElemLabels]

  inline given deriveFromProduct[A:CmdLineDefault](using mirrorOfProd:Mirror.ProductOf[A], behavior:CmdLineBehavior):FromCmdLine[A] =
    new FromCmdLine[A] {
      val names   : List[String] = labelsOf[A]
      val parsers : List[FromCmdLine[_]] = summonAll[mirrorOfProd.MirroredElemTypes]
      val defValues = summon[CmdLineDefault[A]]

      def undefinedKey(keyName:String):Unit = 
        behavior.undefinedKey(keyName)

      def unexpectedInput(arg:String):Unit = 
        behavior.unexpectedInput(arg)

      def parseError(keyName:String, error:String):Unit = 
        behavior.parseError(keyName, error)

      def parse(args: List[String]): Either[String, (A, List[String])] = 
        val keyParsers: Map[String,FromCmdLine[?]] = names.zip(parsers).toMap
        var values: Map[String,Any] = Map.empty
        var cmdLine = args
        var stop = false
        while ! stop do
          val expectNames = names.toSet.diff(values.keySet)

          if cmdLine.isEmpty || expectNames.isEmpty
          then stop = true
          else
            if cmdLine.head.startsWith("-") && cmdLine.head.size>1
            then
              val fieldName = cmdLine.head.substring(1)
              val keyName = cmdLine.head
              cmdLine = cmdLine.tail
              val parserOpt = keyParsers.get(fieldName)
              if parserOpt.isDefined
              then 
                val parser = parserOpt.get.asInstanceOf[FromCmdLine[Any]]
                parser.parse(cmdLine) match
                  case Left(error) => parseError(keyName,error)
                  case Right((value, nextCmdLine)) =>
                    cmdLine = nextCmdLine
                    values = values + (fieldName -> value)
              else undefinedKey(keyName)
            else
              //unexpectedInput(cmdLine.head)
              //cmdLine = cmdLine.tail
              stop = true

        val notFoundNames = names.toSet.diff(values.keySet)
        notFoundNames.foreach { fieldName => 
          val parser = keyParsers(fieldName).asInstanceOf[FromCmdLine[Any]]
          parser.parse(defValues.cmdLineDefault(fieldName)).foreach { case ((value,_)) => 
            values = values + (fieldName -> value)
          }
        }

        val expectNames = names.toSet.diff(values.keySet)

        if expectNames.isEmpty then
          val valueList = names.map { fieldName => values(fieldName) }
          val product:Product = new Product {
            override def productArity: Int = valueList.size
            override def productElement(n: Int): Any = valueList(n)
            override def canEqual(that: Any): Boolean = false
          }
          val resultValue = mirrorOfProd.fromProduct(product)
          Right(resultValue,cmdLine)
        else
          Left(s"not parsed, expect keys: ${expectNames.map(n => "-"+n)}, actual values: ${values}")
    }