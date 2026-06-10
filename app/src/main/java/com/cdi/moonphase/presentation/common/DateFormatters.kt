package com.cdi.moonphase.presentation.common

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Single source of truth for the app's locale-aware date formatting. */
val PT_BR: Locale = Locale("pt", "BR")

private val FULL_DATE = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT_BR)
private val SHORT_DATE = DateTimeFormatter.ofPattern("d 'de' MMM", PT_BR)
private val LONG_DATE = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", PT_BR)
private val MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy", PT_BR)

/** e.g. "Domingo, 14 de junho". */
fun LocalDate.formatFull(): String =
    format(FULL_DATE).replaceFirstChar { it.titlecase(PT_BR) }

/** e.g. "14 de jun". */
fun LocalDate.formatShort(): String = format(SHORT_DATE)

/** e.g. "14 de junho de 2026". */
fun LocalDate.formatLong(): String = format(LONG_DATE)

/** e.g. "Junho 2026". */
fun YearMonth.formatMonthYear(): String =
    format(MONTH_YEAR).replaceFirstChar { it.titlecase(PT_BR) }
