package xyz.cofe.scl3

import org.junit.Test

// https://blog.philipp-martini.de/blog/magic-mirror-scala3/

import scala.deriving.Mirror

trait PrettyString[A] {
  def prettyString(a: A): String // will print a type A in a pretty, human readable way
}

//val intPrettyString =
//  new PrettyString[Int] {
//    def prettyString(a: Int): String  = a.toString
//  }
//
//val stringPrettyString = new PrettyString[String] {
//  def prettyString(a: String): String = "\"" + a + "\""
//}
//
//val booleanPrettyString = new PrettyString[Boolean] {
//  def prettyString(a: Boolean): String = a.toString
//}

given intPrettyString: PrettyString[Int] with
  def prettyString(a: Int): String = a.toString

given longPrettyString: PrettyString[Long] with
  def prettyString(a: Long): String = a.toString

given stringPrettyString: PrettyString[String] with
  def prettyString(a: String): String = "\"" + a + "\""

given booleanPrettyString: PrettyString[Boolean] with
  def prettyString(a: Boolean): String = a.toString

enum SiteMember:
  case RegisteredUser(id: Long, email: String, isAdmin: Boolean)
  case AnonymousUser(session: String)
  
import scala.compiletime.constValue
inline def labelFromMirror[A](using m: Mirror.Of[A]): String = constValue[m.MirroredLabel]

import scala.compiletime.erasedValue
inline def getElemLabels[A <: Tuple]: List[String] = inline erasedValue[A] match {
  case _: EmptyTuple => Nil // stop condition - the tuple is empty
  case _: (head *: tail) =>  // yes, in scala 3 we can match on tuples head and tail to deconstruct them step by step
    val headElementLabel = constValue[head].toString // bring the head label to value space
    val tailElementLabels = getElemLabels[tail] // recursive call to get the labels from the tail
    headElementLabel :: tailElementLabels // concat head + tail
}

// helper method to get the mirror from compiler
inline def getElemLabelsHelper[A](using m: Mirror.Of[A]) =
  getElemLabels[m.MirroredElemLabels] // and call getElemLabels with the elemlabels type

import scala.compiletime.summonInline
inline def getTypeclassInstances[A <: Tuple]: List[PrettyString[Any]] = inline erasedValue[A] match {
  case _: EmptyTuple => Nil
  case _: (head *: tail) =>
    val headTypeClass = summonInline[PrettyString[head]] // summon was known as implicitly in scala 2
    val tailTypeClasses = getTypeclassInstances[tail] // recursive call to resolve also the tail
    headTypeClass.asInstanceOf[PrettyString[Any]] :: getTypeclassInstances[tail]
}

// helper method like before
inline def summonInstancesHelper[A](using m: Mirror.Of[A]) =
  getTypeclassInstances[m.MirroredElemTypes]

inline def derivePrettyStringCaseClass[A](using m: Mirror.ProductOf[A]) =
  new PrettyString[A] {
    def prettyString(a: A): String = {
      val label = labelFromMirror[m.MirroredType]
      val elemLabels = getElemLabels[m.MirroredElemLabels]
      val elemInstances = getTypeclassInstances[m.MirroredElemTypes]
      val elems = a.asInstanceOf[Product].productIterator // every case class implements scala.Product, we can safely cast here
      val elemStrings = elems.zip(elemLabels).zip(elemInstances).map{
        case ((elem, label), instance) => s"$label=${instance.prettyString(elem)}"
      }
      //s"$label(${elemStrings.mkString(", ")})"
      if (elemLabels.isEmpty) { // check if this is a case object (or parameterless case class)
        label
      } else {
        s"$label(${elemStrings.mkString(", ")})"
      }
    }
  }

inline def derivePrettyStringSealedTrait[A](using m: Mirror.SumOf[A]) =
  new PrettyString[A] {
    def prettyString(a: A): String = {
      // val label = labelFromMirror[m.MirroredType] - not needed
      // val elemLabels = getElemLabels[m.MirroredElemLabels] - not needed
      val elemInstances = getTypeclassInstances[m.MirroredElemTypes] // same as for the case class
      val elemOrdinal = m.ordinal(a) // Checks the ordinal of the type, e.g. 0 for User or 1 for AnonymousVisitor
      
      // just return the result of prettyString from the right element instance
      elemInstances(elemOrdinal).prettyString(a)
    }
  }


inline given derived[A](using m: Mirror.Of[A]): PrettyString[A] =
  inline m match {
    case s: Mirror.SumOf[A]     => derivePrettyStringSealedTrait(using s)
    case p: Mirror.ProductOf[A] => derivePrettyStringCaseClass(using p)
  }

enum Visitor {
  case User(name: String, age: Int)
  case AnonymousVisitor
}

class DerivTest {
  import Visitor._
  
  @Test
  def test1: Unit = {
    println("hello")
    
    val regUsr = SiteMember.RegisteredUser(10,"email",false)
    println(regUsr)
    println(labelFromMirror[SiteMember])
    println(labelFromMirror[SiteMember.RegisteredUser])
    println(getElemLabelsHelper[SiteMember.RegisteredUser])
    
    val fields = summonInstancesHelper[SiteMember.RegisteredUser]
    println(fields)
  
    val userPrettyString = derivePrettyStringCaseClass[User]
    println(userPrettyString.prettyString(User("Bob",12)))
  
    val visitorPrettyString = derived[Visitor]
    println(
      visitorPrettyString.prettyString(User("Boba",14))
    )
    println(
      visitorPrettyString.prettyString(AnonymousVisitor)
    )
    
//    val sm = derivePrettyStringCaseClass[SiteMember]
//    println(sm.prettyString(regUsr))
  }
}
