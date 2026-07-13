package ai.hnu.kr.termproject_navercalander_duplicate.data

import kotlinx.coroutines.flow.Flow

class ScheduleRepository(
    private val scheduleDao: ScheduleDao,
    private val categoryDao: CategoryDao
) {
    // Schedule operations
    fun getSchedulesByMonth(start: String, end: String): Flow<List<ScheduleEntity>> = 
        scheduleDao.getSchedulesInDateRange(start, end)

    fun getSchedulesByDate(date: String): Flow<List<ScheduleEntity>> = 
        scheduleDao.getSchedulesByDate(date)

    fun getDDaySchedules(): Flow<List<ScheduleEntity>> = 
        scheduleDao.getDDaySchedules()

    suspend fun insert(schedule: ScheduleEntity): Long {
        return scheduleDao.insertSchedule(schedule)
    }

    suspend fun delete(schedule: ScheduleEntity) {
        scheduleDao.deleteSchedule(schedule)
    }

    // Category operations
    fun getAllCategories(): Flow<List<CategoryEntity>> = 
        categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }
}
