package com.barrita.android.mainapp.app.util

import java.util.Calendar

/**
 * Parses store schedule in format "Lunes: 10:00 - 14:00, Martes: 09:00 - 18:00"
 * or "Siempre Abierto". Mirrors logic from frontend store-hours.ts.
 */
object StoreHoursHelper {

    private val DAY_MAP = mapOf(
        Calendar.SUNDAY to "Domingo",
        Calendar.MONDAY to "Lunes",
        Calendar.TUESDAY to "Martes",
        Calendar.WEDNESDAY to "Miércoles",
        Calendar.THURSDAY to "Jueves",
        Calendar.FRIDAY to "Viernes",
        Calendar.SATURDAY to "Sábado"
    )

    data class StoreOpenStatus(
        val isOpen: Boolean,
        val todayHours: String?
    )

    fun isStoreOpen(schedule: String?): StoreOpenStatus {
        if (schedule.isNullOrBlank() || schedule.trim() == "Siempre Abierto") {
            return StoreOpenStatus(isOpen = true, todayHours = null)
        }

        val entries = parseSchedule(schedule)
        if (entries.isEmpty()) {
            return StoreOpenStatus(isOpen = true, todayHours = null)
        }

        val cal = Calendar.getInstance()
        val currentDay = DAY_MAP[cal.get(Calendar.DAY_OF_WEEK)] ?: return StoreOpenStatus(true, null)
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        val todayEntries = entries.filter { it.day.equals(currentDay, ignoreCase = true) }
        if (todayEntries.isEmpty()) {
            return StoreOpenStatus(isOpen = false, todayHours = null)
        }

        val hoursText = todayEntries.joinToString(", ") { "${it.startTime} - ${it.endTime}" }
        val isOpen = todayEntries.any { entry ->
            val start = timeToMinutes(entry.startTime)
            val end = timeToMinutes(entry.endTime)
            if (start != null && end != null) {
                currentMinutes in start..end
            } else false
        }

        return StoreOpenStatus(isOpen = isOpen, todayHours = hoursText)
    }

    private data class ScheduleEntry(val day: String, val startTime: String, val endTime: String)

    private fun parseSchedule(schedule: String): List<ScheduleEntry> {
        val parts = schedule.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val regex = Regex("""^(.+?):\s*(\d{1,2}:\d{2})\s*-\s*(\d{1,2}:\d{2})\s*$""")
        return parts.mapNotNull { part ->
            regex.matchEntire(part)?.let { match ->
                ScheduleEntry(
                    day = match.groupValues[1].trim(),
                    startTime = match.groupValues[2],
                    endTime = match.groupValues[3]
                )
            }
        }
    }

    private fun timeToMinutes(time: String): Int? {
        val parts = time.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return h * 60 + m
    }
}
