package xyz.cofe.jtfm.bg.copy

trait CopyStreamListener:
  def started():Unit
  def progress(writed:Long):Unit
  def stopped():Unit