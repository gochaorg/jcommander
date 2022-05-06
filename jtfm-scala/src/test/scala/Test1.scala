import org.junit.Test
import org.junit.Assert.*

class Test1:
  @Test def t1(): Unit =
    val l = List( "1", "2", "aa", "3" )
    val x = l.flatMap(_.toIntOption)
    val y = for {
      i <- x;
      j <- x
    } yield (i,j)
    y foreach { (a,b) => println(s"${a} ${b}") }
