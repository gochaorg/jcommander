package xyz.cofe.files

import xyz.cofe.json4s3.derv._
import java.nio.file.Path
import xyz.cofe.files.FilesOperation._
import xyz.cofe.jtfm.json._
import java.time.Instant
import xyz.cofe.jtfm.json.given

class LogTest extends munit.FunSuite {
  test("FilesOperation 2 json") {
    val op : FilesOperation = IsDirectory(Path.of("/some"),FilesOption.defaultOption.copy)
    println(
      op.json
    )
  }
}