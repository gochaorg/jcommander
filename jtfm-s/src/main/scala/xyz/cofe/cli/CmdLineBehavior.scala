package xyz.cofe.cli

trait CmdLineBehavior:
  def undefinedKey(keyName:String):Unit
  def unexpectedInput(arg:String):Unit
  def parseError(keyName:String, error:String):Unit

object CmdLineBehavior:
  given CmdLineBehavior with
    def undefinedKey(keyName:String):Unit = 
      println(s"undefinedKey $keyName")

    def unexpectedInput(arg:String):Unit = 
      println(s"unexpectedInput $arg")

    def parseError(keyName:String, error:String):Unit = 
      println(s"parseError $keyName error $error")
