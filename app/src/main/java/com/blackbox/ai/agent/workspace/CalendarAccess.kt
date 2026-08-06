package com.blackbox.ai.agent.workspace

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.blackbox.ai.agent.workspace.FeatureAccessStore.Companion.FEATURE_CALENDAR
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.TimeZone

/**
 * Kai-style calendar read/write, gated by FeatureAccessStore. The assistant can
 * only create/read events when the user granted CALENDAR access in Agent Hub.
 *
 * Call [createEvent] from a UI/activity context so runtime permissions can be
 * requested; the daemon alone cannot request permissions.
 */
class CalendarAccess(
    private val context: Context,
    private val featureAccess: FeatureAccessStore,
) {

    sealed class CalendarResult {
        data class Success(val eventId: Long, val title: String, val startTime: String) : CalendarResult()
        data class Error(val message: String) : CalendarResult()
    }

    fun hasCalendarPermission(): Boolean {
        val read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
        val write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
        return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
    }

    fun isGranted(): Boolean = featureAccess.isGranted(FEATURE_CALENDAR)

    fun getPrimaryCalendarId(): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.IS_PRIMARY,
        )
        val selection = "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?"
        val selectionArgs = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Calendars.IS_PRIMARY} DESC",
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                if (idIndex >= 0) return cursor.getLong(idIndex)
            }
        }
        return null
    }

    fun createEvent(
        title: String,
        startTimeIso: String,
        endTimeIso: String? = null,
        description: String? = null,
        location: String? = null,
        allDay: Boolean = false,
        reminderMinutes: Int = 0,
    ): CalendarResult {
        if (!isGranted()) {
            return CalendarResult.Error("Calendar not granted. Enable Calendar in Agent Hub feature access.")
        }
        if (!hasCalendarPermission()) {
            return CalendarResult.Error("Calendar permission missing. Grant it in Android Settings (or run from the app to request it).")
        }
        val calendarId = getPrimaryCalendarId()
            ?: return CalendarResult.Error("No writable calendar found. Add a calendar account on the device.")

        val startMillis = try {
            parseIsoDateTimeToEpochMs(startTimeIso)
        } catch (e: DateTimeParseException) {
            return CalendarResult.Error("Invalid start time: ${e.message}")
        }
        val endMillis = if (endTimeIso != null) {
            try {
                parseIsoDateTimeToEpochMs(endTimeIso)
            } catch (e: DateTimeParseException) {
                return CalendarResult.Error("Invalid end time: ${e.message}")
            }
        } else {
            startMillis + 60 * 60 * 1000
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.ALL_DAY, if (allDay) 1 else 0)
            description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
            location?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
        }

        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
            if (eventId != null) {
                if (reminderMinutes > 0) addReminder(eventId, reminderMinutes)
                CalendarResult.Success(eventId, title, formatForDisplay(startMillis))
            } else {
                CalendarResult.Error("Failed to create calendar event")
            }
        } catch (e: Exception) {
            CalendarResult.Error("Error creating event: ${e.message}")
        }
    }

    private fun addReminder(eventId: Long, minutesBefore: Int) {
        val reminderValues = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, minutesBefore)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        runCatching {
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        }
    }

    private fun parseIsoDateTimeToEpochMs(isoString: String): Long {
        val trimmed = isoString.trim()
        try {
            return OffsetDateTime.parse(trimmed).toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) {
        }
        try {
            return Instant.parse(trimmed).toEpochMilli()
        } catch (_: DateTimeParseException) {
        }
        val formatters = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        )
        for (formatter in formatters) {
            try {
                return LocalDateTime.parse(trimmed, formatter)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {
            }
        }
        throw DateTimeParseException("Unable to parse date: $isoString", isoString, 0)
    }

    private fun formatForDisplay(millis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a")
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).format(formatter)
    }
}
