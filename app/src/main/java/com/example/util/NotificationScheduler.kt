package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.*

object NotificationScheduler {

    const val WORKOUT_PREP_ID = 2001
    const val WORKOUT_ACTION_ID = 2002
    const val WORKOUT_RECOVERY_ID = 2003
    const val MEAL1_ID = 2004
    const val MEAL2_ID = 2005
    const val SNACK_ID = 2006
    const val SLEEP_WIND_DOWN_ID = 2007
    const val RISK_WINDOW_ID = 2008

    fun scheduleAlarm(
        context: Context,
        id: Int,
        timeMs: Long,
        title: String,
        text: String,
        type: String
    ) {
        // Don't schedule in the past
        if (timeMs <= System.currentTimeMillis()) {
            Log.w("NotificationScheduler", "Skipping alarm schedule in past: id=$id, title=$title, timeMs=$timeMs")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("text", text)
            putExtra("type", type)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            }
            Log.d("NotificationScheduler", "Scheduled alarm: id=$id, title=$title, time=${Date(timeMs)}")
        } catch (e: SecurityException) {
            // Fallback for exact alarm permission not granted
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            Log.w("NotificationScheduler", "SecurityException scheduling exact alarm, falling back to setAndAllowWhileIdle", e)
        }
    }

    fun cancelAlarm(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("NotificationScheduler", "Cancelled alarm: id=$id")
        }
    }

    fun cancelAllReforgeAlarms(context: Context) {
        val ids = listOf(
            WORKOUT_PREP_ID, WORKOUT_ACTION_ID, WORKOUT_RECOVERY_ID,
            MEAL1_ID, MEAL2_ID, SNACK_ID, SLEEP_WIND_DOWN_ID, RISK_WINDOW_ID
        )
        for (id in ids) {
            cancelAlarm(context, id)
        }
    }

    fun getEpochMsForTimeToday(timeStr: String): Long {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If it was already past for today, schedule it for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}
