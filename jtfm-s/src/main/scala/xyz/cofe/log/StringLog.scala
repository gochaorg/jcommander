package xyz.cofe.log

import org.slf4j.Logger

extension (sc: StringContext)
  private def mkMsg(args:Any*):String =
    val stringContextIterator = sc.parts.iterator
    val argsIterator = args.iterator
    val sb = new java.lang.StringBuilder(stringContextIterator.next())
    while (argsIterator.hasNext) {
      val arg = argsIterator.next()
      sb.append { 
        if arg!=null then
          arg.toString 
        else
          "null"
      }
      sb.append(stringContextIterator.next())
    }
    sb.toString()

  def trace(args:Any*)(using logger:Logger):Unit =
    logger.trace(mkMsg(args:_*))

  def debug(args:Any*)(using logger:Logger):Unit =
    logger.debug(mkMsg(args:_*))

  def log(args:Any*)(using logger:Logger):Unit =    
    logger.info(mkMsg(args:_*))

  def warn(args:Any*)(using logger:Logger):Unit =
    logger.warn(mkMsg(args:_*))

  def error(args:Any*)(using logger:Logger):Unit =
    logger.error(mkMsg(args:_*))
