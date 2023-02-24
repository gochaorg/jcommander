package xyz.cofe.jtfm.bg.exch

import xyz.cofe.log._
import org.slf4j.Logger
import org.slf4j.LoggerFactory

trait NonRelativeResponse[ID,A]:
  def accept(id:ID, response:A):Unit

object NonRelativeResponse:
  private implicit val logger:Logger = LoggerFactory.getLogger("xyz.cofe.jtfm.bg.NonRelativeResponse")
  given [ID,A]: NonRelativeResponse[ID,A] with
    override def accept(id: ID, response: A): Unit = 
      warn"accept id=$id response=$response"