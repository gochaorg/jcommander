package xyz.cofe.jtfm.bg.copy

import java.nio.file.Path

trait CopyLog:
  def copy[R](from:Path,to:Path)(code: =>R):R = code  
  def copySymbolicLink[R](from:Path,to:Path)(code: =>R):R = code  
  def copyDirectory[R](from:Path,to:Path)(code: =>R):R = code  
  def copyRegularFile[R](from:Path,to:Path)(code: =>R):R = code  
  def notImplemented(message:String):Unit
  def error(message:String, err:Throwable):Unit
