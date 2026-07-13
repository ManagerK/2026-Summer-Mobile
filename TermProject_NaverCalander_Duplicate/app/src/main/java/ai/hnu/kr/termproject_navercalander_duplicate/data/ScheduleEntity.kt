package ai.hnu.kr.termproject_navercalander_duplicate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val memo: String? = null,
    val date: String, // Keep for backward compatibility and quick input (start date)
    val startTime: String? = null, // format: 'HH:mm'
    val endDate: String? = null, // format: 'YYYY-MM-DD'
    val endTime: String? = null, // format: 'HH:mm'
    val isRecurringYearly: Boolean = false,
    val categoryColor: String,
    val isDDay: Boolean,
    val isQuickMemo: Boolean = false,
    val notificationTime: String? = null, // Single notification time for Quick Input
    val notificationOffsets: String? = null, // JSON list of minutes (e.g., [10, 60, 1440]) for Full Input
    val photoPaths: String? = null
)
