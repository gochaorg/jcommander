package xyz.cofe.jtfm.files

import java.nio.file.Path

class PathExtTest extends munit.FunSuite {
  test("readDir") {
    val curDir = Path.of(".")
    println(curDir)
    println(curDir.name)
    println(curDir.canonical)
    curDir.canonical.parent.foreach( prnt => println(prnt))
    curDir.readDir.foreach( ls => 
      ls.foreach( p =>
        println(p.name) 
      )
    )
  }
}
