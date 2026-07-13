package ai.hnu.kr.termproject_navercalander_duplicate.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("SELECT * FROM schedules WHERE date <= :monthEnd AND (COALESCE(endDate, date) >= :monthStart OR (isRecurringYearly = 1 AND SUBSTR(COALESCE(endDate, date), 6, 5) >= SUBSTR(:monthStart, 6, 5)))")
    fun getSchedulesInDateRange(monthStart: String, monthEnd: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE (:selectedDate BETWEEN date AND COALESCE(endDate, date)) OR (isRecurringYearly = 1 AND SUBSTR(:selectedDate, 6, 5) BETWEEN SUBSTR(date, 6, 5) AND COALESCE(SUBSTR(endDate, 6, 5), SUBSTR(date, 6, 5)))")
    fun getSchedulesByDate(selectedDate: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE isDDay = 1 ORDER BY date ASC")
    fun getDDaySchedules(): Flow<List<ScheduleEntity>>
}
