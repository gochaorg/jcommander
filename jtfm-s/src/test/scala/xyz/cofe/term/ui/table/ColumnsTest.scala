package xyz.cofe.term.ui.table

import java.nio.file.Path
import xyz.cofe.files._

trait Value[A,B]:
  def read(a:A):B

object Value:
  def apply[A,B]( f:A=>B ):Value[A,B] =
    new Value[A,B] {
      override def read(a: A): B = f(a)
    }

object ColumnSet:
  given name: Value[Path,String] = path => path.name
  given ext:  Value[Path,Option[String]] = path => path.`extension`
  given size: Value[Path,Option[Long]] = path => path.size.map(Some(_)).getOrElse(None)

case class Colum[R,V](
  id:String, 
  read:Value[R,V]
)

class ColumnsTest extends munit.FunSuite:
  test(""){
  }
