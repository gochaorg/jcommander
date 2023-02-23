package xyz.cofe.jtfm.bg.copy

trait CancelSignal:
  def listen( listener: =>Unit ):Unit
  def send():Unit
