package xyz.cofe.files.jnr

import jnr.constants.platform.Errno

enum PosixError:
  case Common(errno:Errno, extraData:Option[String])
  case Method(errno:Errno, methodName:Option[String], extraData:Option[String])
  case Unimplemented(methodName:Option[String])
  case Warning(message: Option[String], data: Array[Object])
