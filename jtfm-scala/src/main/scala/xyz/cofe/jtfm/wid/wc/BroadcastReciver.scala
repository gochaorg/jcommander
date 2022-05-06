package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.input.KeyStroke

/**
 * Принимает событие нажатия клавиш, которые не обработаны фокусом ввода
 * Применяется для компонентов (виджетов)
 */
trait BroadcastReciver {
  def reciveBroadcast( ks:KeyStroke ):Unit
}
