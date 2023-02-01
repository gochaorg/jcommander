package xyz.cofe.jtfm

import org.slf4j.LoggerFactory

object HelloMessage:
  def writeLog:Unit =
    val logger = LoggerFactory.getLogger("jtfm")
    logger.info("startup jtfm")
    System.getProperties().stringPropertyNames().forEach( propName => 
      Option(System.getProperties().getProperty(propName)).foreach( propValue =>
        logger.info("system property {} = {}", propName, propValue)
      )
    )
