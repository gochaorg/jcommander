package xyz.cofe.files

import java.util.regex.Pattern
import java.nio.file.Path

enum PathFilter:
  case Wildcard(wildcard:String) extends PathFilter with PathFilter.WildcardOps(wildcard)
  def apply(path:Path):Boolean = this match
    case w : Wildcard => 
      val m = w.pattern.matcher(path.toString())
      m.matches()

object PathFilter:
  enum Chr:
    case Regex(str:String)
    case Plain(str:String)

  trait WildcardOps(wildcard:String):
    lazy val pattern:Pattern =
      Pattern.compile(
        wildcard.flatMap { 
          case '?' => List(Chr.Regex("."))
          case '*' => List(Chr.Regex(".*"))
          case c   => List(Chr.Plain(c.toString()))
        }.foldLeft(List.empty[Chr]){ case (lst,chr) => 
          lst.headOption match
            case Some(value) => value match
              case Chr.Regex(str1) => chr match
                case Chr.Regex(str2) => Chr.Regex(str1 + str2) :: lst.tail
                case Chr.Plain(str) =>  chr :: lst
              case Chr.Plain(str1) => chr match
                case Chr.Regex(str) => chr :: lst
                case Chr.Plain(str2) => Chr.Plain(str1 + str2) :: lst.tail
            case None => List(chr)
        }.reverse.map {
          case Chr.Plain(str) => Chr.Regex(Pattern.quote(str))
          case c => c
        }.map {
          case Chr.Regex(str) => str
          case Chr.Plain(str) => str
        }.mkString
      )
