package xyz.cofe.jtfm.bg.copy

opaque type BufferSize = Int

object BufferSize:
  def apply(value:Int):BufferSize = value

extension (bsize:BufferSize)
  def intValue:Int = bsize
