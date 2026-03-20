package com.barrita.android.mainapp.app.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Backend espera [createdAt] en ISO 8601 (@IsDateString).
 * El SDK Point suele devolver fechas locales tipo "20/03/2026".
 */
object PaymentConfirmationDateFormatter {

    fun toIso8601OrNull(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val s = raw.trim()

        // Ya parece ISO (yyyy-MM-dd o con T)
        if (s.length >= 10 && s[0].isDigit() && s[4] == '-' && s[7] == '-') {
            return if (s.contains('T')) s else "${s.take(10)}T00:00:00.000Z"
        }

        return try {
            val parser = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            parser.isLenient = false
            val date = parser.parse(s) ?: return null
            val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            iso.timeZone = TimeZone.getTimeZone("UTC")
            iso.format(date)
        } catch (_: Exception) {
            null
        }
    }
}
