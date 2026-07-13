package ai.hnu.kr.termproject_navercalander_duplicate.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ai.hnu.kr.termproject_navercalander_duplicate.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ScheduleRepository
    
    val dDaySchedules: StateFlow<List<ScheduleEntity>>
    
    private val _currentMonthStr = MutableStateFlow(LocalDate.now().toString().substring(0, 7))
    val monthSchedules: StateFlow<List<ScheduleEntity>>

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val selectedDateSchedules: StateFlow<List<ScheduleEntity>>
    
    val categories: StateFlow<List<CategoryEntity>>

    private val _targetScheduleIdFromIntent = MutableStateFlow<Int?>(null)
    val targetScheduleIdFromIntent: StateFlow<Int?> = _targetScheduleIdFromIntent.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ScheduleRepository(db.scheduleDao(), db.categoryDao())
        
        dDaySchedules = repository.getDDaySchedules()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
            
        monthSchedules = _currentMonthStr.flatMapLatest { monthStr ->
            val yearMonth = java.time.YearMonth.parse(monthStr)
            val start = yearMonth.atDay(1).toString()
            val end = yearMonth.atEndOfMonth().toString()
            repository.getSchedulesByMonth(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        selectedDateSchedules = _selectedDate.flatMapLatest { date ->
            repository.getSchedulesByDate(date.toString())
        }.map { list ->
            list.sortedWith(
                compareByDescending<ScheduleEntity> { it.isQuickMemo }
                    .thenByDescending { it.isDDay }
                    .thenBy { it.title }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        categories = repository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Initial default categories
        viewModelScope.launch {
            repository.insertCategory(CategoryEntity("Study", "#3F51B5"))
            repository.insertCategory(CategoryEntity("Exercise", "#4CAF50"))
            repository.insertCategory(CategoryEntity("Rest", "#FF9800"))
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        _currentMonthStr.value = date.toString().substring(0, 7)
    }

    fun updateMonth(yearMonthStr: String) {
        _currentMonthStr.value = yearMonthStr
    }

    fun addSchedule(
        id: Int = 0,
        title: String,
        memo: String?,
        date: LocalDate,
        startTime: String? = null,
        endDate: LocalDate? = null,
        endTime: String? = null,
        isRecurringYearly: Boolean = false,
        categoryColor: String,
        isDDay: Boolean,
        notificationTime: String? = null,
        notificationOffsets: List<Int>? = null,
        photoPaths: List<String>? = null
    ) {
        viewModelScope.launch {
            val entity = ScheduleEntity(
                id = id,
                title = title,
                memo = memo,
                date = date.toString(),
                startTime = startTime,
                endDate = endDate?.toString(),
                endTime = endTime,
                isRecurringYearly = isRecurringYearly,
                categoryColor = categoryColor,
                isDDay = isDDay,
                notificationTime = notificationTime,
                notificationOffsets = notificationOffsets?.let { com.google.gson.Gson().toJson(it) },
                photoPaths = photoPaths?.let { com.google.gson.Gson().toJson(it) }
            )
            
            // If updating, cancel old notifications first
            if (id != 0) {
                ai.hnu.kr.termproject_navercalander_duplicate.notification.NotificationScheduler.cancelNotification(
                    getApplication(),
                    id
                )
            }
            
            val savedId = repository.insert(entity)
            val finalId = if (id == 0) savedId.toInt() else id
            
            ai.hnu.kr.termproject_navercalander_duplicate.notification.NotificationScheduler.scheduleNotification(
                getApplication(),
                entity.copy(id = finalId)
            )
        }
    }
    
    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.delete(schedule)
            ai.hnu.kr.termproject_navercalander_duplicate.notification.NotificationScheduler.cancelNotification(
                getApplication(),
                schedule.id
            )
        }
    }

    fun addCategory(name: String, color: String) {
        viewModelScope.launch {
            repository.insertCategory(CategoryEntity(name, color))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun addQuickMemo(id: Int = 0, memo: String, date: LocalDate) {
        viewModelScope.launch {
            repository.insert(
                ScheduleEntity(
                    id = id,
                    title = memo,
                    date = date.toString(),
                    categoryColor = "#808080", // Grey for quick memo
                    isDDay = false,
                    isQuickMemo = true
                )
            )
        }
    }

    fun setTargetScheduleId(id: Int?) {
        _targetScheduleIdFromIntent.value = id
    }
}
