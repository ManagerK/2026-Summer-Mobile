package ai.hnu.kr.termproject_navercalander_duplicate.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ai.hnu.kr.termproject_navercalander_duplicate.data.ScheduleEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationScheduler {
    // List of all possible offset values to ensure clean cancellation
    private val ALL_OFFSETS = listOf(0, 10, 60, 180, 720, 1440, 4320, 10080, 43200)

    fun scheduleNotification(context: Context, schedule: ScheduleEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // 1. Handle Quick Input style (single notificationTime)
        schedule.notificationTime?.let { timeStr ->
            val triggerAt = calculateTriggerAt(schedule.date, timeStr)
            if (triggerAt > System.currentTimeMillis()) {
                setAlarm(context, alarmManager, schedule.id, 9, schedule.title, triggerAt)
            }
        }

        // 2. Handle Full Input style (multiple offsets)
        schedule.notificationOffsets?.let { offsetsJson ->
            val offsets: List<Int> = Gson().fromJson(offsetsJson, object : TypeToken<List<Int>>() {}.type)
            val startTimeStr = schedule.startTime ?: "09:00"
            val baseDateTime = LocalDateTime.of(LocalDate.parse(schedule.date), LocalTime.parse(startTimeStr))
            
            offsets.forEach { minutesBefore ->
                val triggerDateTime = baseDateTime.minusMinutes(minutesBefore.toLong())
                val triggerAt = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                if (triggerAt > System.currentTimeMillis()) {
                    val index = ALL_OFFSETS.indexOf(minutesBefore)
                    setAlarm(context, alarmManager, schedule.id, index, schedule.title, triggerAt)
                }
            }
        }
    }

    private fun calculateTriggerAt(dateStr: String, timeStr: String): Long {
        val date = LocalDate.parse(dateStr)
        val time = LocalTime.parse(timeStr)
        val dateTime = LocalDateTime.of(date, time)
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun setAlarm(context: Context, alarmManager: AlarmManager, scheduleId: Int, index: Int, title: String, triggerAt: Long) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", "일정 알림: $title")
            putExtra("scheduleId", scheduleId)
        }

        // Create a stable, safe requestCode: scheduleId * 10 + index
        // This supports up to ~200 million schedules and 10 alarms per schedule
        val requestCode = scheduleId * 10 + index
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAt, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelNotification(context: Context, scheduleId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        
        // Cancel all potential 10 alarms (0-8 for offsets, 9 for quick input)
        for (i in 0..9) {
            val requestCode = scheduleId * 10 + i
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
