package xyz.cofe.files.jnr

/////////////////////////////

opaque type UserID = Int

object UserID:
  def apply(value:Int):UserID = value

extension (usr:UserID)
  def usrValue:Int = usr

////////////////////////////////

opaque type GroupID = Int

object GroupID:
  def apply(value:Int):GroupID = value

extension (usr:GroupID)
  def grpValue:Int = usr

////////////////////////////////

opaque type StatFileTime = Long

object StatFileTime:
  def apply(value:Long):StatFileTime = value

extension (usr:StatFileTime)
  def grpValue:Long = usr
