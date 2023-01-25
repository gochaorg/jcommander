package xyz.cofe.files

import java.nio.file.Path
import java.time.Instant

import xyz.cofe.json4s3.derv._
import xyz.cofe.files.FilesOperation._

import xyz.cofe.jtfm.json._
import xyz.cofe.jtfm.json.given

import FilesOperation.given
import xyz.cofe.files.given

import xyz.cofe.json4s3.stream.ast.AST

class LogTest extends munit.FunSuite {
  // given ToJson[FilesOperation] with
  //  override def toJson(t: FilesOperation): Option[AST] = ???

  test("FilesOperation 2 json") {
    val op : FilesOperation = IsDirectory(Path.of("/some"),FilesOption.defaultOption.copy)

    println(
      op.json
    )
  }
}