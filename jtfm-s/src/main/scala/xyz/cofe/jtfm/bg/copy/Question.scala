package xyz.cofe.jtfm.bg.copy

trait Question:
  def quesion[R:TextOf]( questionText:String, variantA:R, variants:R* ):R

