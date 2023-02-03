package xyz.cofe.jtfm.ui.table

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.LocalDateTime
import xyz.cofe.term.ui.table.CellText

object timeRender:
  val fullDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val dateOnlyFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val timeOnlyFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

  def toLocalDateTime(time:Instant):LocalDateTime = time.atZone(ZoneId.systemDefault()).toLocalDateTime()
  def isCurrentDate(time:Instant):Boolean =
    val date = toLocalDateTime(time)
    val now = LocalDateTime.now(ZoneId.systemDefault())
    date.getYear() == now.getYear() && date.getMonthValue() == now.getMonthValue() && date.getDayOfMonth() == now.getDayOfMonth()

  def shortCellValue(value: Instant):String = 
    toLocalDateTime(value).format(
      if isCurrentDate(value) then timeOnlyFormat else dateOnlyFormat
    )

  given shortCellEitherValue:CellText[Either[Throwable,Instant]] with
    override def cellTextOf(value: Either[Throwable, Instant]): String = 
      value.map { t => shortCellValue(t) }.getOrElse("")