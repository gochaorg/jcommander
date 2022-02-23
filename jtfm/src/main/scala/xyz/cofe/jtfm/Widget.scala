package xyz.cofe.jtfm

/**
 * Виджет - визуальный элемент для рендера и управления данными
 */
trait Widget[SELF <: Widget[SELF]]
extends
  Parent[SELF,Widget[SELF]] // Свойство parent
  , Nested[SELF,_ <: Widget[_]] // Дочерние объекты
  
