package xyz.cofe.term.ui

trait FocusManager:
  def owner:Option[WidgetInput]
  def next:Option[WidgetInput]
