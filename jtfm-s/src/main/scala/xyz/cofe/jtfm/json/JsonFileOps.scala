package xyz.cofe.jtfm.json

import java.nio.file.Path
import xyz.cofe.files._
import xyz.cofe.json4s3.derv._
import xyz.cofe.json4s3.derv.errors.DervError
import java.nio.charset.StandardCharsets

extension (path:Path)(using log:FilesLogger, opts:FilesOption)
  def readJsonAs[A:FromJson]:Either[JsonFileError,A] =
    path.readString(StandardCharsets.UTF_8).left.map(JsonFileError.IOErr(_)).flatMap(str => str.jsonAs[A].left.map(JsonFileError.DervErr(_)))

enum JsonFileError:
  case DervErr(err:DervError)
  case IOErr(err:Throwable)
