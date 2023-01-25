package xyz.cofe.cli

import scala.util.Try
import scala.util.Failure
import scala.util.Success
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.Duration
import java.util.regex.Pattern
import java.nio.file.Path

trait PredefCmdLineParsers:
  given FromCmdLine[String] with
    def parse(args: List[String]): Either[String, (String, List[String])] = 
      if args.isEmpty 
      then Left("no data")
      else Right((args.head,args.tail))

  given FromCmdLine[Boolean] with
    def parse(args: List[String]): Either[String, (Boolean, List[String])] = 
      if args.isEmpty 
      then Left("no data")
      else args.head.toLowerCase() match
        case "true"  | "on"  | "yes" => Right(true,args.tail)
        case "false" | "off" | "no"  => Right(false,args.tail)
        case _ => Left(s"can't parse boolean from ${args.head}")

  given FromCmdLine[Int] with
    def parse(args: List[String]): Either[String, (Int, List[String])] = 
      if args.isEmpty 
      then Left("no data")
      else 
        Try(args.head.toInt) match
          case Failure(exception) => Left(exception.toString())
          case Success(value) => Right(value,args.tail)

  given FromCmdLine[Long] with
    def parse(args: List[String]): Either[String, (Long, List[String])] = 
      if args.isEmpty 
      then Left("no data")
      else 
        Try(args.head.toLong) match
          case Failure(exception) => Left(exception.toString())
          case Success(value) => Right(value,args.tail)

  given FromCmdLine[Double] with
    def parse(args: List[String]): Either[String, (Double, List[String])] = 
      if args.isEmpty 
      then Left("no data")
      else 
        Try(args.head.toDouble) match
          case Failure(exception) => Left(exception.toString())
          case Success(value) => Right(value,args.tail)

  given FromCmdLine[Path] with
    def parse(args: List[String]): Either[String, (Path, List[String])] = 
      if args.isEmpty 
      then Left("no data")
      else 
        Try(Path.of(args.head)) match
          case Failure(exception) => Left(exception.toString())
          case Success(value) => Right(value,args.tail)

  given FromCmdLine[Instant] with
    val patterns = List(
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
      DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    )

    def parse(args: List[String]): Either[String, (Instant, List[String])] = 
      if args.isEmpty 
      then Left("no data")
      else 
        Try(Instant.parse(args.head)) match
          case Failure(exception) => 
            val localDt = patterns.foldLeft( None:Option[LocalDateTime] ){ case ( res, fmt ) => 
              res match
                case None => 
                  Try(LocalDateTime.parse(args.head, fmt)) match
                    case Failure(exception) => None
                    case Success(value) => 
                      Some(value)
                case s@Some(value) => s
            }
            localDt.map( dt => 
              Right( dt.toInstant(ZoneOffset.UTC), args.tail )
            ).getOrElse(
              Left(exception.toString())
            )
          case Success(value) => Right(value,args.tail)

  given FromCmdLine[Duration] with
    val hourRegex = Pattern.compile("(\\d+)\\s*h.*")
    val minRegex  = Pattern.compile("(\\d+)\\s*m.*")
    val secRegex  = Pattern.compile("(\\d+)\\s*s.*")
    def parse(str:String, ptrn:Pattern):Option[Long] =
      val m = ptrn.matcher(str)
      if m.matches() 
      then {
        Try(m.group(1).toLong) match
          case Failure(exception) => None
          case Success(value) => Some(value)
        }
      else 
        None

    def parseHours(str:String) = parse(str, hourRegex).map( n => Duration.ofHours(n) )
    def parseMinutes(str:String) = parse(str, minRegex).map( n => Duration.ofMinutes(n) )
    def parseSeconds(str:String) = parse(str, secRegex).map( n => Duration.ofSeconds(n) )
    def parseDuration(str:String) = 
      parseHours(str)
      .orElse(parseMinutes(str))
      .orElse(parseSeconds(str))

    def parse(args: List[String]): Either[String, (Duration, List[String])] = 
      if args.isEmpty 
      then Left("no data")
      else
        parseDuration(args.head) match
          case Some(value) => Right(value, args.tail)
          case None => Left("can't parse Duration")
      