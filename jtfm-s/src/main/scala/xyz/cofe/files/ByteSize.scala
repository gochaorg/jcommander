package xyz.cofe.files

import java.util.regex.Pattern

opaque type ByteSize = Long

object ByteSize:
  def apply(size:Long):ByteSize = size
  def apply(size:Int):ByteSize = size.toLong

extension (size:ByteSize)
  def value:Long = size
  def bytePart:Int  = (size % 1024).toInt

  def kbytePart:Int = ((value >> 10) % 1024).toInt
  def kbytes:Long = value >> 10

  def mbytePart:Int = ((value >> 20) % 1024).toInt
  def mbytes:Long = value >> 20

  def gbytePart:Int = ((value >> 30) % 1024).toInt
  def gbytes:Long = value >> 30
  
  def tbytePart:Int = ((value >> 40) % 1024).toInt
  def tbytes:Long = value >> 40

  def pbytePart:Int = ((value >> 50) % 1024).toInt
  def humanReadable:HumanReadable =
    if pbytePart>0   then HumanReadable( pbytePart.toDouble + (tbytePart.toDouble / 1024.0), "p" )
    else if tbytes>0 then HumanReadable( tbytePart.toDouble + (gbytePart.toDouble / 1024.0), "t" )
    else if gbytes>0 then HumanReadable( gbytePart.toDouble + (mbytePart.toDouble / 1024.0), "g" )
    else if mbytes>0 then HumanReadable( mbytePart.toDouble + (kbytePart.toDouble / 1024.0), "m" )
    else if kbytes>0 then HumanReadable( kbytePart.toDouble + (bytePart.toDouble / 1024.0), "k" )
    else HumanReadable( value.toDouble, "" )

  def parts:List[SizeSuff] =
    List(
        SizeSuff.PByte(pbytePart)
      , SizeSuff.TByte(tbytePart)
      , SizeSuff.GByte(gbytePart)
      , SizeSuff.MByte(mbytePart)
      , SizeSuff.MByte(kbytePart)
      , SizeSuff.Byte(bytePart)
    )

  def precisionReadable:String =
    parts.filter(_.count > 0).map(p => s"${p.count}${p.name.head}").mkString(" ")

case class HumanReadable(value:Double, suff:String):
  override def toString(): String = 
    val str = value.toString()
    val i = str.indexOf(".")
    if i>=0 then
      val head = str.take(i+1)
      val sub = str.substring(i+1)
      head + sub.take(2) + suff
    else
      str + suff

enum SizeSuff(val multiplier:Long, val name:List[String]):
  case Byte(count:Long)  extends SizeSuff(1,List("b"))
  case KByte(count:Long) extends SizeSuff(1024,List("k","kb"))
  case MByte(count:Long) extends SizeSuff(1024L*1024L,List("m","mb"))
  case GByte(count:Long) extends SizeSuff(1024L*1024L*1024L,List("g","gb"))
  case TByte(count:Long) extends SizeSuff(1024L*1024L*1024L*1024L,List("t","tb"))
  case PByte(count:Long) extends SizeSuff(1024L*1024L*1024L*1024L*1024L,List("p","pb"))
  def count:Long

object SizeSuff:
  private def digitOf(c:Char):Option[Int] =
    c match
      case '0' => Some(0)
      case '1' => Some(1)
      case '2' => Some(2)
      case '3' => Some(3)
      case '4' => Some(4)
      case '5' => Some(5)
      case '6' => Some(6)
      case '7' => Some(7)
      case '8' => Some(8)
      case '9' => Some(9)
      case _   => None
    
  private val firstSuffLetter = List('k','m','g','t','p')
  private val pattern = Pattern.compile("(?i)(?<m>\\s*(?<num>\\d+)\\s*(?<suf>([kmgtp]b?|b)?)).*")

  def parse(string:String, offset:Int):Option[(SizeSuff,Int)] =
    if offset<0 || offset>=string.length() then None
    val m = pattern.matcher(string.substring(offset))
    if m.matches() then
      val suff = Option(m.group("suf")).map { str => 
        str.toLowerCase() match
          case "b" | ""   => (n:Long) => SizeSuff.Byte(n)
          case "k" | "kb" => (n:Long) => SizeSuff.KByte(n)
          case "m" | "mb" => (n:Long) => SizeSuff.MByte(n)
          case "g" | "gb" => (n:Long) => SizeSuff.GByte(n)
          case "t" | "tb" => (n:Long) => SizeSuff.TByte(n)
          case "p" | "pb" => (n:Long) => SizeSuff.PByte(n)
      }.getOrElse( (n:Long) => SizeSuff.Byte(n) )
      val sz = suff( m.group("num").toLong )
      val matchSize = m.group("m").length()
      Some(sz,offset + matchSize)
    else
      None

  def parseSizes(string:String,offset:Int):Option[(List[SizeSuff],Int)] =
    val lst = Iterator.iterate( parse(string,offset).map(r=>(true,List(r))).getOrElse(false,List.empty) ){ case (succ,res) =>
      if succ && res.lastOption.isDefined then
        val (_,nextOff) = res.head
        val pr = parse(string,nextOff)
        pr match
          case None => 
            (false,res)
          case Some(value) => 
            (true,value :: res)    
      else
        (false,res)
    }.takeWhile{ 
      case (succ,res) => 
        succ 
    }
    .toList.map( (succ,res) => res )

    if lst.nonEmpty then
      val res0 = lst.last
      res0.map(_._2).maxOption.map { off =>
        ( res0.map(_._1).reverse, off )
      }
    else
      None

  def parseSizes(string:String):Option[List[SizeSuff]] =
    parseSizes(string,0).map(_._1)
