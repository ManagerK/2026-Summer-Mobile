package ai.hnu.kr.termproject_navercalander_duplicate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.hnu.kr.termproject_navercalander_duplicate.data.CategoryEntity
import ai.hnu.kr.termproject_navercalander_duplicate.data.ScheduleEntity
import ai.hnu.kr.termproject_navercalander_duplicate.ui.CalendarViewModel
import coil.compose.AsyncImage
import java.io.File
import android.app.Activity
import android.content.ContextWrapper
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.*
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

enum class AppThemeColor(val primary: Color, val container: Color) {
    DEFAULT(Color(0xFF03C75A), Color(0xFFE8F5E9)),
    ORANGE(Color(0xFFFF9800), Color(0xFFFFF3E0)),
    BLUE(Color(0xFF2196F3), Color(0xFFE3F2FD)),
    GRAY(Color(0xFF607D8B), Color(0xFFECEFF1)),
    RED(Color(0xFFF44336), Color(0xFFFFEBEE)),
    PURPLE(Color(0xFF9C27B0), Color(0xFFF3E5F5))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        
        setContent {
            var isDarkMode by remember { 
                mutableStateOf(sharedPref.getBoolean("dark_mode", false)) 
            }
            var themeColor by remember { 
                mutableStateOf(
                    AppThemeColor.valueOf(sharedPref.getString("theme_color", "DEFAULT") ?: "DEFAULT")
                ) 
            }

            val viewModel: CalendarViewModel = viewModel()
            
            // Handle Intent from Notification
            LaunchedEffect(intent) {
                val scheduleId = intent.getIntExtra("scheduleId", -1)
                if (scheduleId != -1) {
                    viewModel.setTargetScheduleId(scheduleId)
                }
            }
            
            NaverCalendarTheme(darkTheme = isDarkMode, appThemeColor = themeColor) {
                MainScreen(
                    viewModel = viewModel,
                    onThemeChange = { 
                        isDarkMode = it
                        sharedPref.edit().putBoolean("dark_mode", it).apply()
                    },
                    onColorChange = { 
                        themeColor = it
                        sharedPref.edit().putString("theme_color", it.name).apply()
                    },
                    currentDarkMode = isDarkMode,
                    currentThemeColor = themeColor
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CalendarViewModel = viewModel(),
    onThemeChange: (Boolean) -> Unit,
    onColorChange: (AppThemeColor) -> Unit,
    currentDarkMode: Boolean,
    currentThemeColor: AppThemeColor
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var showDialog by remember { mutableStateOf(false) }
    var showQuickMemoDialog by remember { mutableStateOf(false) }
    var showFullInputDialog by remember { mutableStateOf(false) }
    var showYearMonthPicker by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showCreatorInfo by remember { mutableStateOf(false) }
    var showDeskClock by rememberSaveable { mutableStateOf(false) }
    
    var selectedDetailSchedule by remember { mutableStateOf<ScheduleEntity?>(null) }
    var editingSchedule by remember { mutableStateOf<ScheduleEntity?>(null) }
    var scheduleToDelete by remember { mutableStateOf<ScheduleEntity?>(null) }
    
    val selectedDate by viewModel.selectedDate.collectAsState()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    val monthSchedules by viewModel.monthSchedules.collectAsState()
    val daySchedules by viewModel.selectedDateSchedules.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val targetScheduleId by viewModel.targetScheduleIdFromIntent.collectAsState()

    val context = LocalContext.current
    val activity = remember(context) {
        var c = context
        while (c is ContextWrapper) {
            if (c is Activity) return@remember c as Activity
            c = c.baseContext
        }
        null
    }

    LaunchedEffect(showDeskClock) {
        if (!showDeskClock) {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Show Detail Dialog if targetScheduleId is set from Notification
    LaunchedEffect(targetScheduleId, monthSchedules) {
        targetScheduleId?.let { id ->
            monthSchedules.find { it.id == id }?.let { schedule ->
                selectedDetailSchedule = schedule
                viewModel.setTargetScheduleId(null) // Reset after showing
            }
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(android.Manifest.permission.CAMERA)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "TaskCalander+",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                NavigationDrawerItem(
                    label = { Text("탁상 시계 모드") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showDeskClock = true
                    },
                    icon = { Icon(Icons.Default.Alarm, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("앱 설정") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSettings = true
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("앱 제작자") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showCreatorInfo = true
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TaskCalander+") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = { 
                            editingSchedule = null
                            showQuickMemoDialog = true 
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Quick Memo")
                    }
                    FloatingActionButton(onClick = { 
                        editingSchedule = null
                        showFullInputDialog = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Schedule")
                    }
                }
            }
        ) { innerPadding ->
            BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                val isExpanded = this.maxWidth > 600.dp
                
                if (isExpanded) {
                    // Large screen (Tablet / Foldable) - Two-pane layout
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left Pane: Calendar
                        Surface(
                            modifier = Modifier.weight(1.1f).fillMaxHeight(),
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column {
                                CalendarHeader(
                                    currentMonth = currentMonth,
                                    onPreviousMonth = { 
                                        currentMonth = currentMonth.minusMonths(1)
                                        viewModel.updateMonth(currentMonth.toString())
                                    },
                                    onNextMonth = { 
                                        currentMonth = currentMonth.plusMonths(1)
                                        viewModel.updateMonth(currentMonth.toString())
                                    },
                                    onTitleClick = { showYearMonthPicker = true }
                                )
                                CalendarGrid(
                                    currentMonth = currentMonth,
                                    schedules = monthSchedules,
                                    selectedDate = selectedDate,
                                    onDateClick = { viewModel.setSelectedDate(it) },
                                    onDateLongClick = {
                                        viewModel.setSelectedDate(it)
                                        showDialog = true
                                    },
                                    modifier = Modifier.fillMaxSize().padding(bottom = 16.dp)
                                )
                            }
                        }
                        
                        // Right Pane: Schedule List
                        Column(modifier = Modifier.weight(0.9f).fillMaxHeight().padding(horizontal = 16.dp)) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "${selectedDate.dayOfMonth}일 ${selectedDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.KOREAN)}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (daySchedules.isEmpty()) "일정 없음" else "${daySchedules.size}개의 일정",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (daySchedules.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "등록된 일정이 없습니다.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                ScheduleList(
                                    schedules = daySchedules,
                                    onDelete = { scheduleToDelete = it },
                                    onItemClick = { selectedDetailSchedule = it }
                                )
                            }
                        }
                    }
                } else {
                    // Small screen (Phone) - Single-pane layout
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column {
                                CalendarHeader(
                                    currentMonth = currentMonth,
                                    onPreviousMonth = { 
                                        currentMonth = currentMonth.minusMonths(1)
                                        viewModel.updateMonth(currentMonth.toString())
                                    },
                                    onNextMonth = { 
                                        currentMonth = currentMonth.plusMonths(1)
                                        viewModel.updateMonth(currentMonth.toString())
                                    },
                                    onTitleClick = { showYearMonthPicker = true }
                                )
                                CalendarGrid(
                                    currentMonth = currentMonth,
                                    schedules = monthSchedules,
                                    selectedDate = selectedDate,
                                    onDateClick = { viewModel.setSelectedDate(it) },
                                    onDateLongClick = {
                                        viewModel.setSelectedDate(it)
                                        showDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth().height(320.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${selectedDate.dayOfMonth}일 ${selectedDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.KOREAN)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (daySchedules.isEmpty()) "일정 없음" else "${daySchedules.size}개의 일정",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (daySchedules.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "새로운 일정을 등록해보세요!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                ScheduleList(
                                    schedules = daySchedules,
                                    onDelete = { scheduleToDelete = it },
                                    onItemClick = { selectedDetailSchedule = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            currentDarkMode = currentDarkMode,
            currentThemeColor = currentThemeColor,
            onDismiss = { showSettings = false },
            onThemeChange = onThemeChange,
            onColorChange = onColorChange
        )
    }

    if (showCreatorInfo) {
        CreatorInfoDialog(onDismiss = { showCreatorInfo = false })
    }

    if (showYearMonthPicker) {
        YearMonthPickerDialog(
            currentMonth = currentMonth,
            onDismiss = { showYearMonthPicker = false },
            onConfirm = { year, month ->
                currentMonth = YearMonth.of(year, month)
                viewModel.updateMonth(currentMonth.toString())
                showYearMonthPicker = false
            }
        )
    }

    if (showQuickMemoDialog) {
        QuickMemoDialog(
            initialMemo = editingSchedule?.title ?: "",
            onDismiss = { 
                showQuickMemoDialog = false
                editingSchedule = null
            },
            onSave = { memo ->
                viewModel.addQuickMemo(
                    id = editingSchedule?.id ?: 0,
                    memo = memo, 
                    date = if (editingSchedule != null) LocalDate.parse(editingSchedule!!.date) else selectedDate
                )
                showQuickMemoDialog = false
                editingSchedule = null
            }
        )
    }

    if (showDialog) {
        QuickInputDialog(
            categories = categories,
            date = selectedDate,
            initialSchedule = editingSchedule,
            onDismiss = { 
                showDialog = false
                editingSchedule = null
            },
            onSave = { id, title, memo, categoryColor, isDDay, notificationTime, photoPaths ->
                viewModel.addSchedule(
                    id = id,
                    title = title,
                    memo = memo,
                    date = selectedDate,
                    categoryColor = categoryColor,
                    isDDay = isDDay,
                    notificationTime = notificationTime,
                    photoPaths = photoPaths
                )
                showDialog = false
                editingSchedule = null
            },
            onAddCategory = { name, color ->
                viewModel.addCategory(name, color)
            },
            onDeleteCategory = { category ->
                viewModel.deleteCategory(category)
            }
        )
    }

    if (showFullInputDialog) {
        FullInputDialog(
            categories = categories,
            date = selectedDate,
            initialSchedule = editingSchedule,
            onDismiss = { 
                showFullInputDialog = false
                editingSchedule = null
            },
            onSave = { id, title, memo, startDate, startTime, endDate, endTime, isRecurring, categoryColor, isDDay, offsets, photoPaths ->
                viewModel.addSchedule(
                    id = id,
                    title = title,
                    memo = memo,
                    date = startDate,
                    startTime = startTime,
                    endDate = endDate,
                    endTime = endTime,
                    isRecurringYearly = isRecurring,
                    categoryColor = categoryColor,
                    isDDay = isDDay,
                    notificationOffsets = offsets,
                    photoPaths = photoPaths
                )
                showFullInputDialog = false
                editingSchedule = null
            },
            onAddCategory = { name, color ->
                viewModel.addCategory(name, color)
            },
            onDeleteCategory = { category ->
                viewModel.deleteCategory(category)
            }
        )
    }

    selectedDetailSchedule?.let { schedule ->
        ScheduleDetailDialog(
            schedule = schedule,
            categories = categories,
            onDismiss = { selectedDetailSchedule = null },
            onEdit = {
                editingSchedule = schedule
                selectedDetailSchedule = null
                
                if (schedule.isQuickMemo) {
                    showQuickMemoDialog = true
                } else {
                    // Decide which dialog to show based on schedule properties
                    val isFullInput = schedule.endDate != null || 
                                     schedule.startTime != null || 
                                     schedule.notificationOffsets != null || 
                                     schedule.isRecurringYearly
                    
                    if (isFullInput) {
                        showFullInputDialog = true
                    } else {
                        showDialog = true
                    }
                }
            }
        )
    }

    if (scheduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("일정 삭제") },
            text = { Text("이 일정을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    scheduleToDelete?.let { viewModel.deleteSchedule(it) }
                    scheduleToDelete = null
                }) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) {
                    Text("취소")
                }
            }
        )
    }

    if (showDeskClock) {
        DeskClockScreen(
            schedules = monthSchedules,
            todaySchedules = daySchedules.filter { it.date == LocalDate.now().toString() },
            categories = categories,
            onExit = { showDeskClock = false }
        )
    }
}

@Composable
fun DeskClockScreen(
    schedules: List<ScheduleEntity>,
    todaySchedules: List<ScheduleEntity>,
    categories: List<CategoryEntity>,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) {
        var c = context
        while (c is ContextWrapper) {
            if (c is Activity) return@remember c as Activity
            c = c.baseContext
        }
        null
    }
    val configuration = LocalConfiguration.current
    val view = LocalView.current
    
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var burnInOffset by remember { mutableStateOf(Offset.Zero) }
    var showOverlay by rememberSaveable { mutableStateOf(false) }
    
    var ambientLux by remember { mutableFloatStateOf(100f) }
    val isNightTint = ambientLux < 5f
    
    var isActiveBurnInPrevention by rememberSaveable { mutableStateOf(false) }
    var layoutRotationIndex by rememberSaveable { mutableIntStateOf(0) }
    
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // 0. Ambient Light Detection
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.get(0)?.let { ambientLux = it }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // 0. Fullscreen (Immersive Mode) & Screen On Management
    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            
            // Keep screen on
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            val window = activity?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                
                // Restore screen off behavior
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // 1. Digital Clock Update (every second)
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000L)
        }
    }

    // 2. Subtle Pixel Shift (every 60 seconds)
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000L)
            burnInOffset = Offset(x = (-5..5).random().toFloat(), y = (-5..5).random().toFloat())
        }
    }

    // 3. Active Burn-in Prevention: Layout Rotation (every 2 minutes)
    LaunchedEffect(isActiveBurnInPrevention) {
        if (isActiveBurnInPrevention) {
            while (true) {
                delay(120000L) // 2 minutes
                layoutRotationIndex = (layoutRotationIndex + 1) % 3
            }
        } else {
            layoutRotationIndex = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { showOverlay = !showOverlay }
            }
    ) {
        // Dynamic Content Order
        val contents = listOf<@Composable (Modifier) -> Unit>(
            { modifier -> ClockPane(currentTime, isNightTint, modifier) },
            { modifier -> CalendarPane(schedules, isNightTint, modifier) },
            { modifier -> TodaySchedulePane(todaySchedules, isNightTint, modifier) }
        )
        
        val rotatedContents = if (isActiveBurnInPrevention) {
            when (layoutRotationIndex) {
                1 -> listOf(contents[1], contents[2], contents[0])
                2 -> listOf(contents[2], contents[0], contents[1])
                else -> contents
            }
        } else contents

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(translationX = burnInOffset.x, translationY = burnInOffset.y)
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rotatedContents.forEach { content ->
                    content(Modifier.weight(1f))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(translationX = burnInOffset.x, translationY = burnInOffset.y)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                rotatedContents.forEach { content ->
                    content(Modifier.wrapContentHeight())
                }
            }
        }

        // Overlay Menu
        if (showOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onExit,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.width(240.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("탁상 시계 모드 종료")
                    }

                    Surface(
                        color = Color.DarkGray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(240.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("적극적인 번인 방지", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isActiveBurnInPrevention,
                                onCheckedChange = { isActiveBurnInPrevention = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockPane(currentTime: LocalTime, isNightTint: Boolean, modifier: Modifier) {
    val primaryColor = if (isNightTint) Color(0xFFFF4444) else Color.White
    val secondaryColor = if (isNightTint) Color(0xFFAA0000) else Color.Gray

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = currentTime.format(DateTimeFormatter.ofPattern("HH")),
                color = primaryColor,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = ":",
                color = primaryColor.copy(alpha = 0.5f),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 70.sp, fontWeight = FontWeight.Light),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = currentTime.format(DateTimeFormatter.ofPattern("mm")),
                color = primaryColor,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp, fontWeight = FontWeight.ExtraLight)
            )
        }
        Text(
            text = LocalDate.now().format(DateTimeFormatter.ofPattern("M월 d일 E요일", Locale.KOREAN)),
            color = secondaryColor,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun CalendarPane(schedules: List<ScheduleEntity>, isNightTint: Boolean, modifier: Modifier) {
    val accentColor = if (isNightTint) Color(0xFFFF0000) else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currentMonth = YearMonth.now()
        Text(
            text = "${currentMonth.year}.${currentMonth.monthValue}",
            color = accentColor,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        DeskClockCalendarGrid(currentMonth, schedules, isNightTint)
    }
}

@Composable
private fun TodaySchedulePane(todaySchedules: List<ScheduleEntity>, isNightTint: Boolean, modifier: Modifier) {
    val primaryColor = if (isNightTint) Color(0xFFFF4444) else Color.White
    val secondaryColor = if (isNightTint) Color(0xFFAA0000) else Color.DarkGray

    Column(
        modifier = modifier.padding(start = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "오늘의 일정",
            color = primaryColor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        if (todaySchedules.isEmpty()) {
            Text("일정 없음", color = secondaryColor, style = MaterialTheme.typography.bodySmall)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(todaySchedules) { schedule ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (isNightTint) Color(0xFFFF0000) else Color(android.graphics.Color.parseColor(schedule.categoryColor)), 
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (schedule.isQuickMemo) "퀵메모: ${schedule.title}" else schedule.title,
                            color = primaryColor,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeskClockCalendarGrid(currentMonth: YearMonth, schedules: List<ScheduleEntity>, isNightTint: Boolean) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")

    val textColor = if (isNightTint) Color(0xFFFF4444) else Color.White
    val accentColor = if (isNightTint) Color(0xFFFF0000) else MaterialTheme.colorScheme.primary
    val dimTextColor = if (isNightTint) Color(0xFF7F0000) else Color.Gray

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = dimTextColor,
                    fontSize = 10.sp
                )
            }
        }
        val totalCells = 42
        val dates = (0 until totalCells).map { index ->
            val dateIndex = index - firstDayOfMonth
            if (dateIndex in 0 until daysInMonth) currentMonth.atDay(dateIndex + 1) else null
        }

        dates.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1.2f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val isToday = date == LocalDate.now()
                            val dateSchedules = schedules.filter { 
                                val sd = LocalDate.parse(it.date)
                                val ed = it.endDate?.let { end -> LocalDate.parse(end) } ?: sd
                                !date.isBefore(sd) && !date.isAfter(ed)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    color = if (isToday) accentColor else textColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                Row(horizontalArrangement = Arrangement.Center) {
                                    dateSchedules.take(3).forEach { s ->
                                        Box(
                                            modifier = Modifier
                                                .padding(1.dp)
                                                .size(3.dp)
                                                .background(
                                                    if (isNightTint) Color(0xFFFF0000) else Color(android.graphics.Color.parseColor(s.categoryColor)), 
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("앱 제작자 정보") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("이메일", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("developeth82i@gmail.com", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Discord", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("so_bulgogi_02", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Divider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("앱 버전", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1.0 Stable (2026-07-11)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("확인")
            }
        }
    )
}

@Composable
fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTitleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${currentMonth.year}.${currentMonth.monthValue}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onTitleClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Row {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    schedules: List<ScheduleEntity>,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    onDateLongClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // 0 for Sunday
    
    val totalCells = 42 
    val dates = (0 until totalCells).map { index ->
        val dateIndex = index - firstDayOfMonth
        if (dateIndex < 0 || dateIndex >= daysInMonth) null
        else currentMonth.atDay(dateIndex + 1)
    }

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when(day) {
                        "일" -> Color.Red.copy(alpha = 0.7f)
                        "토" -> Color(0xFF29B6F6) // Matte Sky Blue
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = modifier
        ) {
            items(dates) { date ->
                val dateSchedules = schedules.filter { schedule ->
                    val scheduleDate = LocalDate.parse(schedule.date)
                    val scheduleEndDate = schedule.endDate?.let { LocalDate.parse(it) } ?: scheduleDate
                    
                    if (schedule.isRecurringYearly) {
                        date != null && 
                        date.monthValue == scheduleDate.monthValue && 
                        date.dayOfMonth >= scheduleDate.dayOfMonth &&
                        (schedule.endDate == null || (date.monthValue == scheduleEndDate.monthValue && date.dayOfMonth <= scheduleEndDate.dayOfMonth))
                    } else {
                        date != null && !date.isBefore(scheduleDate) && !date.isAfter(scheduleEndDate)
                    }
                }
                DateCell(
                    date = date,
                    schedules = dateSchedules,
                    isSelected = date == selectedDate,
                    onClick = { date?.let { onDateClick(it) } },
                    onLongClick = { date?.let { onDateLongClick(it) } }
                )
            }
        }
    }
}

@Composable
fun DateCell(
    date: LocalDate?,
    schedules: List<ScheduleEntity>,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(date) {
                if (date != null) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { onLongClick() }
                    )
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        if (date != null) {
            val isToday = date == LocalDate.now()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(top = 4.dp)
            ) {
                Box(
                    modifier = if (isToday) Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                    else Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = when {
                            isToday -> MaterialTheme.colorScheme.onPrimary
                            date.dayOfWeek.value == 7 -> Color.Red
                            date.dayOfWeek.value == 6 -> Color(0xFF29B6F6) // Matte Sky Blue
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Mini Color Dots (Phase 4: Real-time Data Visualization)
                Column(
                    modifier = Modifier.padding(bottom = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    val dotColors = schedules.map { it.categoryColor }.distinct().take(9)
                    val rows = dotColors.chunked(3)
                    rows.forEach { rowColors ->
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowColors.forEach { colorHex ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 1.dp)
                                        .size(5.dp)
                                        .background(
                                            Color(android.graphics.Color.parseColor(colorHex)),
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleList(
    schedules: List<ScheduleEntity>,
    onDelete: (ScheduleEntity) -> Unit,
    onItemClick: (ScheduleEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(schedules) { schedule ->
            ScheduleItem(schedule, onDelete, onItemClick)
        }
    }
}

@Composable
fun QuickMemoDialog(
    initialMemo: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var memo by remember { mutableStateOf(initialMemo) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialMemo.isEmpty()) "퀵메모 등록" else "퀵메모 수정") },
        text = {
            TextField(
                value = memo,
                onValueChange = { memo = it },
                placeholder = { Text("내용을 입력하세요") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )
        },
        confirmButton = {
            Button(
                onClick = { if (memo.isNotBlank()) onSave(memo) },
                enabled = memo.isNotBlank()
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun ScheduleItem(
    schedule: ScheduleEntity,
    onDelete: (ScheduleEntity) -> Unit,
    onClick: (ScheduleEntity) -> Unit
) {
    val remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(schedule.date))
    val dDayText = when {
        remainingDays == 0L -> "D-Day"
        remainingDays > 0 -> "D-$remainingDays"
        else -> "D+${-remainingDays}"
    }

    val isUrgentDDay = schedule.isDDay && remainingDays in 0..3
    
    // Quick Memo specific UI
    val isQuickMemo = schedule.isQuickMemo
    
    val photoList = remember(schedule.photoPaths) {
        schedule.photoPaths?.let {
            com.google.gson.Gson().fromJson<List<String>>(it, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type)
        } ?: emptyList()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(schedule) },
        colors = if (isQuickMemo) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        } else if (schedule.isDDay) {
            CardDefaults.cardColors(
                containerColor = if (isUrgentDDay) {
                    if (MaterialTheme.colorScheme.surface == Color.White) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            )
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isQuickMemo) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(android.graphics.Color.parseColor(schedule.categoryColor)), CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isQuickMemo) "Quick Memo" else schedule.title,
                        fontWeight = if (isQuickMemo || schedule.isDDay) FontWeight.ExtraBold else FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (schedule.isDDay) {
                        Text(
                            text = dDayText,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isUrgentDDay) Color.Red else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (schedule.isDDay) {
                    Surface(
                        color = if (isUrgentDDay) Color.Red else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "IMPORTANT",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(onClick = { onDelete(schedule) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.5f))
                }
            }
            
            val displayMemo = if (isQuickMemo) schedule.title else schedule.memo
            if (!displayMemo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayMemo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 22.dp),
                    maxLines = if (isQuickMemo) 3 else 1
                )
            }

            if (photoList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.padding(start = 22.dp)) {
                    photoList.take(3).forEach { path ->
                        AsyncImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (photoList.size > 3) {
                        Text(
                            text = "+${photoList.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleDetailDialog(
    schedule: ScheduleEntity,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val photoList = remember(schedule.photoPaths) {
        schedule.photoPaths?.let {
            com.google.gson.Gson().fromJson<List<String>>(it, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type)
        } ?: emptyList()
    }

    val categoryName = remember(schedule.categoryColor, categories) {
        categories.find { it.color == schedule.categoryColor }?.name ?: "기타"
    }

    val notificationLabels = remember(schedule.notificationOffsets) {
        schedule.notificationOffsets?.let { json ->
            val offsets: List<Int> = com.google.gson.Gson().fromJson(json, object : com.google.gson.reflect.TypeToken<List<Int>>() {}.type)
            val optionsMap = mapOf(
                0 to "시작 시",
                10 to "10분 전",
                60 to "1시간 전",
                180 to "3시간 전",
                720 to "12시간 전",
                1440 to "1일 전",
                4320 to "3일 전",
                10080 to "1주일 전",
                43200 to "한 달 전"
            )
            offsets.mapNotNull { optionsMap[it] }.joinToString(", ")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (schedule.isQuickMemo) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                } else {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(android.graphics.Color.parseColor(schedule.categoryColor)), CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (schedule.isQuickMemo) "퀵메모" else schedule.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 32.sp
                    )
                    if (!schedule.isQuickMemo) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Memo Content
                val memoContent = if (schedule.isQuickMemo) schedule.title else schedule.memo
                if (!memoContent.isNullOrBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (schedule.isQuickMemo) "내용" else "메모",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = memoContent,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                            lineHeight = 28.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 2. Photos (Horizontal Scroll)
                if (photoList.isNotEmpty()) {
                    var zoomedPhotoPath by remember { mutableStateOf<String?>(null) }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "사진 (${photoList.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(photoList) { path ->
                                AsyncImage(
                                    model = path,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { zoomedPhotoPath = path },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    if (zoomedPhotoPath != null) {
                        FullScreenImageViewer(
                            photoPath = zoomedPhotoPath!!,
                            onDismiss = { zoomedPhotoPath = null }
                        )
                    }
                }

                Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 3. Date and Time
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "일시",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "시작: ${schedule.date}${schedule.startTime?.let { " $it" } ?: ""}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        if (schedule.endDate != null) {
                            Text(
                                text = "종료: ${schedule.endDate}${schedule.endTime?.let { " $it" } ?: ""}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        } else if (schedule.endTime != null) {
                            Text(
                                text = "종료: ${schedule.endTime}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
                
                // 4. Important / Recurring Info
                if (schedule.isDDay) {
                    val remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(schedule.date))
                    Surface(
                        color = Color.Red.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "중요한 일정 (${if(remainingDays == 0L) "오늘" else "D-$remainingDays"})",
                            color = Color.Red,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (schedule.isRecurringYearly) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "매년 반복되는 일정", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 5. Notifications
                val fullNotificationText = remember(schedule.notificationTime, notificationLabels) {
                    val list = mutableListOf<String>()
                    schedule.notificationTime?.let { list.add(it) }
                    notificationLabels?.let { if (it.isNotBlank()) list.add(it) }
                    list.joinToString(" / ")
                }

                if (fullNotificationText.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "알림",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = fullNotificationText, 
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        },
        dismissButton = {
            TextButton(onClick = onEdit) {
                Text("수정")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickInputDialog(
    categories: List<CategoryEntity>,
    date: LocalDate,
    initialSchedule: ScheduleEntity? = null,
    onDismiss: () -> Unit,
    onSave: (Int, String, String?, String, Boolean, String?, List<String>?) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialSchedule?.title ?: "") }
    var memo by remember { mutableStateOf(initialSchedule?.memo ?: "") }
    var selectedCategory by remember { 
        mutableStateOf(
            if (initialSchedule != null) {
                categories.find { it.color == initialSchedule.categoryColor } ?: categories.firstOrNull()
            } else {
                categories.firstOrNull()
            }
        ) 
    }
    var isDDay by remember { mutableStateOf(initialSchedule?.isDDay ?: false) }
    
    var notificationTime by remember { mutableStateOf(initialSchedule?.notificationTime) }
    var photoPaths by remember { 
        mutableStateOf(
            initialSchedule?.photoPaths?.let {
                com.google.gson.Gson().fromJson<List<String>>(it, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type)
            } ?: emptyList()
        ) 
    }
    
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    // Color Palette State
    var hue by remember { mutableFloatStateOf(0f) }
    val selectedNewCategoryColor = remember(hue) {
        val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.7f, 0.9f))
        String.format("#%06X", 0xFFFFFF and colorInt)
    }
    
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    photoPaths = photoPaths + file.absolutePath
                }
            }
        }
        tempPhotoFile = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        val remainingSlots = 10 - photoPaths.size
        uris.take(remainingSlots).forEach { uri ->
            val file = File(context.cacheDir, "images/IMG_GALLERY_${System.currentTimeMillis()}_${uri.lastPathSegment}.jpg")
            file.parentFile?.mkdirs()
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                photoPaths = photoPaths + file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun takePicture() {
        if (photoPaths.size >= 10) return
        val file = File(context.cacheDir, "images/IMG_${System.currentTimeMillis()}.jpg").apply {
            parentFile?.mkdirs()
        }
        tempPhotoFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraLauncher.launch(uri)
    }

    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (LocalConfiguration.current.screenWidthDp > 600) 0.8f else 0.95f)
                .padding(16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            BoxWithConstraints(modifier = Modifier.padding(24.dp)) {
                val isWide = this.maxWidth > 500.dp
                
                Column {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("간편 일정 입력", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isWide) {
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                // Left Pane
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedTextField(
                                                value = title,
                                                onValueChange = { title = it },
                                                placeholder = { Text("어떤 일정이 있나요?") },
                                                modifier = Modifier.fillMaxWidth(),
                                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = MaterialTheme.colorScheme.primary)
                                            )
                                            OutlinedTextField(
                                                value = memo,
                                                onValueChange = { memo = it },
                                                placeholder = { Text("상세 메모를 남겨보세요 (선택)") },
                                                modifier = Modifier.fillMaxWidth(),
                                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = MaterialTheme.colorScheme.primary)
                                            )
                                            
                                            Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                            
                                            PhotoSection(
                                                photoPaths = photoPaths,
                                                onPhotoPathsChange = { photoPaths = it },
                                                onTakePicture = { takePicture() },
                                                onPickGallery = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                                            )
                                        }
                                    }
                                    
                                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth().clickable { 
                                                val cal = java.util.Calendar.getInstance()
                                                TimePickerDialog(context, { _, h, m -> notificationTime = String.format(Locale.getDefault(), "%02d:%02d", h, m) }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), true).show()
                                            }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(notificationTime ?: "눌러서 일정 알림 추가", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                                if (notificationTime != null) IconButton(onClick = { notificationTime = null }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            }
                                            Divider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                            Row(modifier = Modifier.fillMaxWidth().clickable { isDDay = !isDDay }.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp), tint = if(isDDay) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("중요한 일정으로 설정", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                                Switch(checked = isDDay, onCheckedChange = { isDDay = it })
                                            }
                                        }
                                    }
                                }

                                // Right Pane
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text("카테고리", style = MaterialTheme.typography.labelLarge)
                                        OutlinedButton(
                                            onClick = { showCategoryDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                selectedCategory?.let {
                                                    Box(modifier = Modifier.size(12.dp).background(Color(android.graphics.Color.parseColor(it.color)), CircleShape))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(it.name)
                                                } ?: Text("카테고리 지정")
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Mobile Layout
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text("어떤 일정이 있나요?") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }, colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = MaterialTheme.colorScheme.primary))
                                    OutlinedTextField(value = memo, onValueChange = { memo = it }, placeholder = { Text("상세 메모를 남겨보세요 (선택)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }, colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = MaterialTheme.colorScheme.primary))
                                    
                                    Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    
                                    PhotoSection(
                                        photoPaths = photoPaths,
                                        onPhotoPathsChange = { photoPaths = it },
                                        onTakePicture = { takePicture() },
                                        onPickGallery = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                                    )
                                }
                            }
                            
                            Column {
                                Text("카테고리", style = MaterialTheme.typography.labelLarge)
                                OutlinedButton(
                                    onClick = { showCategoryDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        selectedCategory?.let {
                                            Box(modifier = Modifier.size(12.dp).background(Color(android.graphics.Color.parseColor(it.color)), CircleShape))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(it.name)
                                        } ?: Text("카테고리 지정")
                                    }
                                }
                            }

                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth().clickable { 
                                        val cal = java.util.Calendar.getInstance()
                                        TimePickerDialog(context, { _, h, m -> notificationTime = String.format(Locale.getDefault(), "%02d:%02d", h, m) }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), true).show()
                                    }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(12.dp))
                                        Text(notificationTime ?: "눌러서 일정 알림 추가", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        if (notificationTime != null) IconButton(onClick = { notificationTime = null }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    }
                                    Divider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    Row(modifier = Modifier.fillMaxWidth().clickable { isDDay = !isDDay }.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp), tint = if(isDDay) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(12.dp)); Text("중요한 일정으로 설정", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        Switch(checked = isDDay, onCheckedChange = { isDDay = it })
                                    }
                                }
                            }
                        }

                        // Shared Buttons (at the end of scrollable column)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("취소") }
                            Button(onClick = { selectedCategory?.let { onSave(initialSchedule?.id ?: 0, title, memo.ifBlank { null }, it.color, isDDay, notificationTime, photoPaths.ifEmpty { null }) } }, enabled = title.isNotBlank() && selectedCategory != null, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("저장") }
                        }
                    }
                }
            }
        }
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("카테고리 삭제") },
            text = { Text("'${categoryToDelete?.name}' 카테고리를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    categoryToDelete?.let {
                        onDeleteCategory(it)
                        if (selectedCategory == it) {
                            selectedCategory = categories.firstOrNull { c -> c != it }
                        }
                    }
                    categoryToDelete = null
                }) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("취소")
                }
            }
        )
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("카테고리 선택") },
            text = {
                Column {
                    CategorySelectionSection(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { 
                            selectedCategory = it
                        },
                        showAddCategory = showAddCategory,
                        onShowAddCategoryChange = { showAddCategory = it },
                        newCategoryName = newCategoryName,
                        onNewCategoryNameChange = { newCategoryName = it },
                        hue = hue,
                        onHueChange = { hue = it },
                        onAddCategory = onAddCategory,
                        selectedNewCategoryColor = selectedNewCategoryColor,
                        onDeleteClick = { categoryToDelete = selectedCategory }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showCategoryDialog = false }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }
}

@Composable
fun FullScreenImageViewer(
    photoPath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset += pan
                    }
                }
        ) {
            AsyncImage(
                model = photoPath,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )
            
            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
fun YearMonthPickerDialog(
    currentMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(currentMonth.year) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth.monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("날짜 이동") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Year Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                    Text(
                        text = "${selectedYear}년",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }

                // Month Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(150.dp)
                ) {
                    items(12) { index ->
                        val month = index + 1
                        val isSelected = selectedMonth == month
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedMonth = month }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${month}월",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedYear, selectedMonth) }) {
                Text("이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentDarkMode: Boolean,
    currentThemeColor: AppThemeColor,
    onDismiss: () -> Unit,
    onThemeChange: (Boolean) -> Unit,
    onColorChange: (AppThemeColor) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("앱 설정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Column {
                    Text("테마 모드", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !currentDarkMode,
                            onClick = { onThemeChange(false) },
                            label = { Text("라이트 모드") }
                        )
                        FilterChip(
                            selected = currentDarkMode,
                            onClick = { onThemeChange(true) },
                            label = { Text("다크 모드") }
                        )
                    }
                }

                Column {
                    Text("테마 색상", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeColor.entries.forEach { theme ->
                            FilterChip(
                                selected = currentThemeColor == theme,
                                onClick = { onColorChange(theme) },
                                label = { 
                                    Text(when(theme) {
                                        AppThemeColor.DEFAULT -> "기본"
                                        AppThemeColor.ORANGE -> "오렌지"
                                        AppThemeColor.BLUE -> "블루"
                                        AppThemeColor.GRAY -> "그레이"
                                        AppThemeColor.RED -> "레드"
                                        AppThemeColor.PURPLE -> "보라"
                                    })
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(theme.primary, CircleShape)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("확인")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FullInputDialog(
    categories: List<CategoryEntity>,
    date: LocalDate,
    initialSchedule: ScheduleEntity? = null,
    onDismiss: () -> Unit,
    onSave: (Int, String, String?, LocalDate, String?, LocalDate?, String?, Boolean, String, Boolean, List<Int>?, List<String>?) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialSchedule?.title ?: "") }
    var memo by remember { mutableStateOf(initialSchedule?.memo ?: "") }
    
    var startDate by remember { mutableStateOf(initialSchedule?.date?.let { LocalDate.parse(it) } ?: date) }
    var startTime by remember { mutableStateOf(initialSchedule?.startTime ?: "09:00") }
    
    var endDate by remember { mutableStateOf(initialSchedule?.endDate?.let { LocalDate.parse(it) }) }
    var endTime by remember { mutableStateOf(initialSchedule?.endTime ?: "10:00") }
    
    var isRecurring by remember { mutableStateOf(initialSchedule?.isRecurringYearly ?: false) }
    
    var selectedCategory by remember { 
        mutableStateOf(
            if (initialSchedule != null) {
                categories.find { it.color == initialSchedule.categoryColor } ?: categories.firstOrNull()
            } else {
                categories.firstOrNull()
            }
        ) 
    }
    var isDDay by remember { mutableStateOf(initialSchedule?.isDDay ?: false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDetailSettingsDialog by remember { mutableStateOf(false) }
    
    val notificationOptions = listOf(
        "시작 시" to 0,
        "10분 전" to 10,
        "1시간 전" to 60,
        "3시간 전" to 180,
        "12시간 전" to 720,
        "1일 전" to 1440,
        "3일 전" to 4320,
        "1주일 전" to 10080,
        "한 달 전" to 43200
    )
    
    var selectedOffsets by remember { 
        mutableStateOf(
            initialSchedule?.notificationOffsets?.let {
                com.google.gson.Gson().fromJson<List<Int>>(it, object : com.google.gson.reflect.TypeToken<List<Int>>() {}.type)
            } ?: emptyList<Int>()
        )
    }
    
    var photoPaths by remember { 
        mutableStateOf(
            initialSchedule?.photoPaths?.let {
                com.google.gson.Gson().fromJson<List<String>>(it, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type)
            } ?: emptyList<String>()
        ) 
    }
    
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    var hue by remember { mutableFloatStateOf(0f) }
    val selectedNewCategoryColor = remember(hue) {
        val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.7f, 0.9f))
        String.format("#%06X", 0xFFFFFF and colorInt)
    }
    
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    photoPaths = photoPaths + file.absolutePath
                }
            }
        }
        tempPhotoFile = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        val remainingSlots = 10 - photoPaths.size
        uris.take(remainingSlots).forEach { uri ->
            val file = File(context.cacheDir, "images/IMG_GALLERY_${System.currentTimeMillis()}_${uri.lastPathSegment}.jpg")
            file.parentFile?.mkdirs()
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                photoPaths = photoPaths + file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun takePicture() {
        if (photoPaths.size >= 10) return
        val file = File(context.cacheDir, "images/IMG_${System.currentTimeMillis()}.jpg").apply {
            parentFile?.mkdirs()
        }
        tempPhotoFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraLauncher.launch(uri)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (LocalConfiguration.current.screenWidthDp > 600) 0.85f else 0.95f)
                .padding(16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            BoxWithConstraints(modifier = Modifier.padding(24.dp)) {
                val isWide = this.maxWidth > 500.dp
                
                Column {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("상세 일정 입력", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isWide) {
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                // Left Column
                                Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("일정 제목") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                                            OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모 (선택 사항)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary), maxLines = 3)
                                            
                                            Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                            
                                            PhotoSection(
                                                photoPaths = photoPaths,
                                                onPhotoPathsChange = { photoPaths = it },
                                                onTakePicture = { takePicture() },
                                                onPickGallery = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                                            )
                                        }
                                    }

                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)), shape = RoundedCornerShape(16.dp)) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text("일시 및 반복", style = MaterialTheme.typography.labelLarge)
                                            OutlinedButton(
                                                onClick = { showDetailSettingsDialog = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                val dateText = if (endDate != null) "$startDate ~ $endDate" else startDate.toString()
                                                val timeText = if (startTime != null) " ($startTime)" else ""
                                                Text("세부 일정 지정: $dateText$timeText")
                                            }
                                        }
                                    }
                                }

                                // Right Column
                                Column(modifier = Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                    Column {
                                        Text("카테고리", style = MaterialTheme.typography.labelLarge)
                                        OutlinedButton(
                                            onClick = { showCategoryDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                selectedCategory?.let {
                                                    Box(modifier = Modifier.size(12.dp).background(Color(android.graphics.Color.parseColor(it.color)), CircleShape))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(it.name)
                                                } ?: Text("카테고리 지정")
                                            }
                                        }
                                    }

                                    Column {
                                        Text("알림 설정", style = MaterialTheme.typography.labelLarge)
                                        OutlinedButton(
                                            onClick = { showNotificationDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            val summary = if (selectedOffsets.isEmpty()) "알림 추가" 
                                                         else selectedOffsets.sorted().mapNotNull { offset -> notificationOptions.find { it.second == offset }?.first }.joinToString(", ")
                                            Text(summary)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Mobile Layout
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                // Info
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("일정 제목") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                                        OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모 (선택 사항)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary), maxLines = 3)
                                        
                                        Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                        
                                        PhotoSection(
                                            photoPaths = photoPaths,
                                            onPhotoPathsChange = { photoPaths = it },
                                            onTakePicture = { takePicture() },
                                            onPickGallery = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                                        )
                                    }
                                }
                                
                                // Date/Time
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)), shape = RoundedCornerShape(16.dp)) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("일시 및 반복", style = MaterialTheme.typography.labelLarge)
                                        OutlinedButton(
                                            onClick = { showDetailSettingsDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            val dateText = if (endDate != null) "$startDate ~ $endDate" else startDate.toString()
                                            Text("세부 일정 지정: $dateText")
                                        }
                                    }
                                }

                                Column {
                                    Text("카테고리", style = MaterialTheme.typography.labelLarge)
                                    OutlinedButton(
                                        onClick = { showCategoryDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            selectedCategory?.let {
                                                Box(modifier = Modifier.size(12.dp).background(Color(android.graphics.Color.parseColor(it.color)), CircleShape))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(it.name)
                                            } ?: Text("카테고리 지정")
                                        }
                                    }
                                }
                                
                                Column {
                                    Text("알림 설정", style = MaterialTheme.typography.labelLarge)
                                    OutlinedButton(
                                        onClick = { showNotificationDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        val summary = if (selectedOffsets.isEmpty()) "알림 추가" 
                                                     else selectedOffsets.sorted().mapNotNull { offset -> notificationOptions.find { it.second == offset }?.first }.joinToString(", ")
                                        Text(summary)
                                    }
                                }
                            }
                        }

                        // Shared Buttons (at the end of scrollable content)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("취소") }
                            Button(onClick = { selectedCategory?.let { onSave(initialSchedule?.id ?: 0, title, memo.ifBlank { null }, startDate, startTime, endDate, endTime, isRecurring, it.color, isDDay, selectedOffsets, photoPaths.ifEmpty { null }) } }, enabled = title.isNotBlank() && selectedCategory != null, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("저장") }
                        }
                    }
                }
            }
        }
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("카테고리 삭제") },
            text = { Text("'${categoryToDelete?.name}' 카테고리를 삭제하시겠습니까?") },
            confirmButton = { TextButton(onClick = { categoryToDelete?.let { onDeleteCategory(it); if (selectedCategory == it) selectedCategory = categories.firstOrNull { c -> c != it } }; categoryToDelete = null }) { Text("삭제", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { categoryToDelete = null }) { Text("취소") } }
        )
    }

    if (showNotificationDialog) {
        var tempSelected by remember { mutableStateOf(selectedOffsets) }
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("미리 알림 선택") },
            text = {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    notificationOptions.forEach { (label, minutes) ->
                        FilterChip(
                            selected = tempSelected.contains(minutes),
                            onClick = {
                                tempSelected = if (tempSelected.contains(minutes)) tempSelected - minutes else tempSelected + minutes
                            },
                            label = { Text(label) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedOffsets = tempSelected
                    showNotificationDialog = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("카테고리 선택") },
            text = {
                Column {
                    CategorySelectionSection(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { 
                            selectedCategory = it
                        },
                        showAddCategory = showAddCategory,
                        onShowAddCategoryChange = { showAddCategory = it },
                        newCategoryName = newCategoryName,
                        onNewCategoryNameChange = { newCategoryName = it },
                        hue = hue,
                        onHueChange = { hue = it },
                        onAddCategory = onAddCategory,
                        selectedNewCategoryColor = selectedNewCategoryColor,
                        onDeleteClick = { categoryToDelete = selectedCategory }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showCategoryDialog = false }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }

    if (showDetailSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailSettingsDialog = false },
            title = { Text("세부 일정 지정") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("시작 일시", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { DatePickerDialog(context, { _, y, m, d -> startDate = LocalDate.of(y, m + 1, d) }, startDate.year, startDate.monthValue - 1, startDate.dayOfMonth).show() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text(startDate.toString()) }
                            OutlinedButton(onClick = { val current = LocalTime.parse(startTime); TimePickerDialog(context, { _, h, min -> startTime = String.format(Locale.getDefault(), "%02d:%02d", h, min) }, current.hour, current.minute, true).show() }, modifier = Modifier.weight(0.7f), shape = RoundedCornerShape(8.dp)) { Text(startTime) }
                        }
                    }
                    Column {
                        Text("종료 일시", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { val current = endDate ?: startDate; DatePickerDialog(context, { _, y, m, d -> endDate = LocalDate.of(y, m + 1, d) }, current.year, current.monthValue - 1, current.dayOfMonth).show() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text(endDate?.toString() ?: "선택 안함") }
                            OutlinedButton(onClick = { val current = LocalTime.parse(endTime); TimePickerDialog(context, { _, h, min -> endTime = String.format(Locale.getDefault(), "%02d:%02d", h, min) }, current.hour, current.minute, true).show() }, modifier = Modifier.weight(0.7f), shape = RoundedCornerShape(8.dp)) { Text(endTime) }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isRecurring = !isRecurring }) {
                        Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                        Text("매년 반복", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isDDay = !isDDay }) {
                        Checkbox(checked = isDDay, onCheckedChange = { isDDay = it })
                        Text("중요한 일정 (D-Day 표시)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDetailSettingsDialog = false }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDetailSettingsDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelectionSection(
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    onCategorySelect: (CategoryEntity) -> Unit,
    showAddCategory: Boolean,
    onShowAddCategoryChange: (Boolean) -> Unit,
    newCategoryName: String,
    onNewCategoryNameChange: (String) -> Unit,
    hue: Float,
    onHueChange: (Float) -> Unit,
    onAddCategory: (String, String) -> Unit,
    selectedNewCategoryColor: String,
    onDeleteClick: () -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("카테고리", style = MaterialTheme.typography.labelLarge)
            }
            Row {
                IconButton(onClick = { onShowAddCategoryChange(!showAddCategory) }) { Icon(if(showAddCategory) Icons.Default.Close else Icons.Default.Add, contentDescription = null) }
                if (!showAddCategory && selectedCategory != null) {
                    IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f)) }
                }
            }
        }
        
        if (showAddCategory) {
            Card(modifier = Modifier.padding(bottom = 12.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = newCategoryName, 
                            onValueChange = onNewCategoryNameChange, 
                            placeholder = { Text("새 카테고리 이름") }, 
                            modifier = Modifier.weight(1f), 
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent, 
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        IconButton(onClick = { if (newCategoryName.isNotBlank()) { onAddCategory(newCategoryName, selectedNewCategoryColor); onNewCategoryNameChange(""); onShowAddCategoryChange(false) } }) { Icon(Icons.Default.Check, contentDescription = null) }
                    }
                    CircularColorPicker(hue = hue, onHueChange = onHueChange, modifier = Modifier.size(100.dp))
                }
            }
        }

        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                InputChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelect(category) },
                    label = { Text(category.name) },
                    leadingIcon = { Box(modifier = Modifier.size(12.dp).background(Color(android.graphics.Color.parseColor(category.color)), CircleShape)) },
                    colors = InputChipDefaults.inputChipColors(selectedContainerColor = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Composable
private fun PhotoSection(
    photoPaths: List<String>,
    onPhotoPathsChange: (List<String>) -> Unit,
    onTakePicture: () -> Unit,
    onPickGallery: () -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("사진 (${photoPaths.size}/10)", style = MaterialTheme.typography.labelLarge)
            }
            Row {
                IconButton(onClick = onTakePicture) { Icon(Icons.Default.PhotoCamera, contentDescription = "촬영") }
                IconButton(onClick = onPickGallery) { Icon(Icons.Default.Collections, contentDescription = "갤러리") }
            }
        }
        if (photoPaths.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(80.dp)) {
                items(photoPaths) { path ->
                    Box {
                        AsyncImage(model = path, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        IconButton(onClick = { onPhotoPathsChange(photoPaths.filter { it != path }) }, modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CircularColorPicker(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val offset = change.position
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = atan2(offset.y - center.y, offset.x - center.x) * (180f / PI.toFloat())
                        val normalizedAngle = if (angle < 0) angle + 360f else angle
                        onHueChange(normalizedAngle)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = atan2(offset.y - center.y, offset.x - center.x) * (180f / PI.toFloat())
                        val normalizedAngle = if (angle < 0) angle + 360f else angle
                        onHueChange(normalizedAngle)
                    }
                }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val innerRadius = (size.minDimension / 2) - 12.dp.toPx()
            
            // Draw Hue Circle
            for (angle in 0 until 360 step 2) {
                val color = Color.hsv(angle.toFloat(), 0.7f, 0.9f)
                drawArc(
                    color = color,
                    startAngle = angle.toFloat(),
                    sweepAngle = 3f,
                    useCenter = false,
                    topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                    size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
                    style = Stroke(width = 16.dp.toPx())
                )
            }

            // Draw Selector Handle
            val handleAngleRad = hue * (PI.toFloat() / 180f)
            val handleX = center.x + innerRadius * cos(handleAngleRad)
            val handleY = center.y + innerRadius * sin(handleAngleRad)
            
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = Offset(handleX, handleY),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = 11.dp.toPx(),
                center = Offset(handleX, handleY),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
fun NaverCalendarTheme(
    darkTheme: Boolean = false,
    appThemeColor: AppThemeColor = AppThemeColor.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = appThemeColor.primary,
            onPrimary = Color.Black,
            primaryContainer = appThemeColor.primary.copy(alpha = 0.2f),
            onPrimaryContainer = appThemeColor.primary,
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onSurface = Color.White,
            onSurfaceVariant = Color(0xFFBDBDBD)
        )
    } else {
        lightColorScheme(
            primary = appThemeColor.primary,
            onPrimary = Color.White,
            primaryContainer = appThemeColor.container,
            onPrimaryContainer = appThemeColor.primary,
            background = Color(0xFFF8F9FA),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF212529),
            onSurfaceVariant = Color(0xFF6C757D)
        )
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            titleLarge = androidx.compose.ui.text.TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = (-0.5).sp
            ),
            titleMedium = androidx.compose.ui.text.TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = (-0.5).sp
            ),
            bodyMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                letterSpacing = (-0.2).sp
            )
        ),
        content = content
    )
}
