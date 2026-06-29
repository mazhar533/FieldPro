package com.mazhar.fieldpro

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.compose.ui.res.painterResource
import com.google.firebase.firestore.ListenerRegistration
import androidx.compose.ui.zIndex
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mazhar.fieldpro.data.*
import com.mazhar.fieldpro.ui.screens.*
import com.mazhar.fieldpro.ui.theme.BackgroundLight
import com.mazhar.fieldpro.ui.theme.YellowPrimary
import com.mazhar.fieldpro.ui.theme.FieldProTheme
import com.mazhar.fieldpro.ui.theme.CardBg
import com.mazhar.fieldpro.ui.theme.YellowText

enum class AppScreen {
    SPLASH,
    LOGIN,
    MAIN,
    JOB_DETAILS,
    SERVICE_REPORT
}

class MainActivity : ComponentActivity() {
    private lateinit var repository: FieldProRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val intentJobId = mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val jobId = intent?.getStringExtra("jobId")
        if (jobId != null) {
            intentJobId.value = jobId
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        repository = FieldProRepository(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()

        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        setContent {
            var isDarkMode by remember { mutableStateOf(repository.isDarkModeEnabled()) }
            FieldProTheme(darkTheme = isDarkMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val coroutineScope = rememberCoroutineScope()
                    var isLoggedIn by remember { mutableStateOf(repository.isUserLoggedIn()) }
                    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
                    var selectedTab by remember { mutableStateOf("Home") }
                    var selectedJobId by remember { mutableStateOf("") }
                    var currentUser by remember { mutableStateOf(repository.getUser()) }

                    LaunchedEffect(currentScreen, isDarkMode) {
                        val window = this@MainActivity.window
                        val view = window.decorView
                        if (currentScreen == AppScreen.SPLASH) {
                            window.statusBarColor = android.graphics.Color.parseColor("#FFD54F")
                            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                        } else {
                            val bgHex = if (isDarkMode) "#121212" else "#FAFAFA"
                            window.statusBarColor = android.graphics.Color.parseColor(bgHex)
                            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
                        }
                    }

                    LaunchedEffect(intentJobId.value, currentUser) {
                        val jobId = intentJobId.value
                        if (jobId != null && jobId.isNotEmpty() && currentUser.role == "TECHNICIAN") {
                            selectedJobId = jobId
                            currentScreen = AppScreen.JOB_DETAILS
                            intentJobId.value = null
                        }
                    }
                    var isTechLoading by remember { mutableStateOf(false) }
                    var jobsInitialTab by remember { mutableStateOf("All") }
                    var showBars by remember { mutableStateOf(isLoggedIn && currentScreen == AppScreen.MAIN) }

                    // Live list states
                    var jobsList by remember { mutableStateOf(repository.getJobs().sortedByDescending { parseCreatedTimestamp(it.createdTimestamp) }) }
                    var previousJobsCount by remember { mutableStateOf(-1) }
                    var notificationsList by remember { mutableStateOf(repository.getNotifications()) }

                    val requestLocationAndExecute = remember {
                        mutableStateOf<((Double?, Double?) -> Unit)?>(null)
                    }
                
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                  permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (granted) {
                        getCurrentLocation { lat, lng ->
                            requestLocationAndExecute.value?.invoke(lat, lng)
                            requestLocationAndExecute.value = null
                        }
                    } else {
                        requestLocationAndExecute.value?.invoke(null, null)
                        requestLocationAndExecute.value = null
                        CustomToastManager.showToast("Location permission is required for verification.", isErrorToast = true)
                    }
                }

                val runWithLocation = { callback: (Double?, Double?) -> Unit ->
                    val hasFine = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasFine || hasCoarse) {
                        getCurrentLocation { lat, lng ->
                            callback(lat, lng)
                        }
                    } else {
                        requestLocationAndExecute.value = callback
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                // Refresh helper
                val refreshData = {
                    if (currentUser.role == "TECHNICIAN") {
                        isTechLoading = true
                        repository.getJobsForTechnician(
                            email = currentUser.email,
                            onSuccess = { list -> 
                                jobsList = list.sortedByDescending { parseCreatedTimestamp(it.createdTimestamp) }
                                notificationsList = repository.getNotifications()
                                isTechLoading = false
                            },
                            onFailure = {
                                isTechLoading = false
                            }
                        )
                    } else {
                        jobsList = repository.getJobs().sortedByDescending { parseCreatedTimestamp(it.createdTimestamp) }
                        notificationsList = repository.getNotifications()
                    }
                }

                // Dynamic Firestore sync for Technicians
                DisposableEffect(isLoggedIn, currentUser) {
                    var listenerReg: ListenerRegistration? = null
                    if (isLoggedIn && currentUser.role == "TECHNICIAN") {
                        isTechLoading = true
                        listenerReg = repository.getJobsForTechnician(
                            email = currentUser.email,
                            onSuccess = { list -> 
                                jobsList = list.sortedByDescending { parseCreatedTimestamp(it.createdTimestamp) }
                                notificationsList = repository.getNotifications()
                                isTechLoading = false
                            },
                            onFailure = {
                                isTechLoading = false
                            }
                        )
                    }
                    onDispose {
                        listenerReg?.remove()
                    }
                }

                var previousNotifCount by remember { mutableStateOf(-1) }
                LaunchedEffect(isLoggedIn) {
                    if (!isLoggedIn) {
                        previousNotifCount = -1
                    }
                }
                LaunchedEffect(notificationsList, isLoggedIn, currentUser) {
                    if (isLoggedIn && currentUser.role == "TECHNICIAN" && notificationsList.isNotEmpty()) {
                        if (previousNotifCount != -1 && notificationsList.size > previousNotifCount) {
                            val newNotif = notificationsList.firstOrNull { !it.isRead }
                            if (newNotif != null && newNotif.title == "New Job Assigned") {
                                val jobId = newNotif.description.substringAfter("assigned ").substringBefore(" for")
                                val job = jobsList.firstOrNull { it.id == jobId }
                                if (job == null || job.status == JobStatus.PENDING) {
                                    this@MainActivity.showSystemNotification(
                                        title = newNotif.title,
                                        message = newNotif.description
                                    )
                                }
                            }
                        }
                        previousNotifCount = notificationsList.size
                    }
                }

                var hasShownTodayReminder by remember { mutableStateOf(false) }
                LaunchedEffect(isLoggedIn) {
                    if (!isLoggedIn) {
                        hasShownTodayReminder = false
                    }
                }
                LaunchedEffect(jobsList, isLoggedIn, currentUser) {
                    if (isLoggedIn && currentUser.role == "TECHNICIAN" && !hasShownTodayReminder && jobsList.isNotEmpty()) {
                        val sdf1 = java.text.SimpleDateFormat("M/d/yyyy", java.util.Locale.getDefault())
                        val sdf2 = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
                        val today1 = sdf1.format(java.util.Date())
                        val today2 = sdf2.format(java.util.Date())
                        
                        val todayJobs = jobsList.filter { 
                            (it.status == JobStatus.ASSIGNED || it.status == JobStatus.IN_PROGRESS) && 
                            (it.serviceDate == today1 || it.serviceDate == today2)
                        }
                        
                        if (todayJobs.isNotEmpty()) {
                            hasShownTodayReminder = true
                            val jobIds = todayJobs.joinToString { it.id }
                            this@MainActivity.showSystemNotification(
                                title = "⏰ Daily Work Reminder",
                                message = "You have ${todayJobs.size} active jobs scheduled for today (${jobIds}). Tap to view details."
                            )
                        }
                    }
                }

                // Handle back pressed or custom back navigation
                val navigateBack: () -> Unit = {
                    if (currentScreen == AppScreen.JOB_DETAILS) {
                        currentScreen = AppScreen.MAIN
                    } else if (currentScreen == AppScreen.SERVICE_REPORT) {
                        currentScreen = AppScreen.JOB_DETAILS
                    }
                }

                BackHandler(enabled = currentScreen != AppScreen.MAIN && currentScreen != AppScreen.LOGIN) {
                    navigateBack()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AnimatedVisibility(
                            visible = showBars && currentScreen == AppScreen.MAIN && currentUser.role == "TECHNICIAN",
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = null,
                                            tint = YellowPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = "FieldPro",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp,
                                            color = YellowPrimary
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = BackgroundLight
                                )
                            )
                        }
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBars && currentScreen == AppScreen.MAIN && currentUser.role == "TECHNICIAN",
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                             BottomNavigationBar(
                                 selectedTab = selectedTab,
                                 onTabSelected = { tab ->
                                     selectedTab = tab
                                     if (tab == "Jobs") {
                                         jobsInitialTab = "All"
                                     }
                                 }
                             )
                        }
                    },
                    containerColor = BackgroundLight
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = if (currentScreen == AppScreen.JOB_DETAILS || currentScreen == AppScreen.SERVICE_REPORT || (currentScreen == AppScreen.MAIN && currentUser.role == "ADMIN")) 0.dp else innerPadding.calculateTopPadding(),
                                bottom = innerPadding.calculateBottomPadding(),
                                start = innerPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                                end = innerPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
                            )
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                            },
                            label = "ScreenTransitions"
                        ) { targetScreen ->
                            when (targetScreen) {
                                AppScreen.SPLASH -> {
                                    SplashScreen(
                                        onSplashFinished = {
                                            if (isLoggedIn) {
                                                val jobId = intentJobId.value
                                                if (jobId != null && jobId.isNotEmpty() && currentUser.role == "TECHNICIAN") {
                                                    selectedJobId = jobId
                                                    currentScreen = AppScreen.JOB_DETAILS
                                                    intentJobId.value = null
                                                } else {
                                                    currentScreen = AppScreen.MAIN
                                                    coroutineScope.launch {
                                                        delay(450)
                                                        showBars = true
                                                    }
                                                }
                                            } else {
                                                currentScreen = AppScreen.LOGIN
                                            }
                                        }
                                    )
                                }
                                AppScreen.LOGIN -> {
                                    LoginScreen(
                                        repository = repository,
                                         onLoginSuccess = { user ->
                                             currentUser = user
                                             isLoggedIn = true
                                             selectedTab = "Home"
                                             refreshData()
                                             currentScreen = AppScreen.MAIN
                                             CustomToastManager.showToast("Logged in successfully!")
                                             coroutineScope.launch {
                                                 delay(450)
                                                 showBars = true
                                             }
                                         }
                                    )
                                }
                                AppScreen.MAIN -> {
                                    if (currentUser.role == "ADMIN") {
                                         var adminOpenJobId by remember { mutableStateOf("") }
                                         LaunchedEffect(intentJobId.value) {
                                             val jobId = intentJobId.value
                                             if (jobId != null && jobId.isNotEmpty()) {
                                                 adminOpenJobId = jobId
                                                 intentJobId.value = null
                                             }
                                         }
                                         AdminDashboardScreen(
                                             repository = repository,
                                             isDarkMode = isDarkMode,
                                             onDarkModeToggle = { enabled ->
                                                 repository.setDarkModeEnabled(enabled)
                                                 isDarkMode = enabled
                                             },
                                             onLogoutClick = {
                                                 showBars = false
                                                 currentScreen = AppScreen.LOGIN
                                                 CustomToastManager.showToast("Logged out successfully!")
                                                 coroutineScope.launch {
                                                     delay(400)
                                                     repository.clearData()
                                                     isLoggedIn = false
                                                     currentUser = repository.getUser()
                                                     selectedTab = "Home"
                                                     refreshData()
                                                 }
                                             },
                                             onShowNotification = { title, message ->
                                                 this@MainActivity.showSystemNotification(title, message)
                                             },
                                             adminOpenJobId = adminOpenJobId,
                                             onAdminOpenJobIdConsumed = {
                                                 adminOpenJobId = ""
                                             }
                                         )
                                     } else {
                                        when (selectedTab) {
                                            "Home" -> {
                                                HomeScreen(
                                                    userName = currentUser.fullName.split(" ").firstOrNull() ?: "Alex",
                                                    jobs = jobsList,
                                                    isLoading = isTechLoading,
                                                    onViewAllJobsClick = { tab ->
                                                        jobsInitialTab = tab
                                                        selectedTab = "Jobs"
                                                    },
                                                    onJobClick = { jobId ->
                                                        selectedJobId = jobId
                                                        currentScreen = AppScreen.JOB_DETAILS
                                                    }
                                                )
                                            }
                                            "Jobs" -> {
                                                JobsScreen(
                                                    jobs = jobsList,
                                                    initialTab = jobsInitialTab,
                                                    isLoading = isTechLoading,
                                                    onJobClick = { jobId ->
                                                        selectedJobId = jobId
                                                        currentScreen = AppScreen.JOB_DETAILS
                                                    }
                                                )
                                            }
                                            "Alerts" -> {
                                                AlertsScreen(
                                                    notifications = notificationsList,
                                                    onNotificationClick = { notifId ->
                                                        repository.markNotificationAsRead(notifId)
                                                        val notif = notificationsList.firstOrNull { it.id == notifId }
                                                        if (notif != null) {
                                                            val jobId = extractJobId(notif.description)
                                                            if (jobId != null) {
                                                                selectedJobId = jobId
                                                                currentScreen = AppScreen.JOB_DETAILS
                                                            }
                                                        }
                                                        refreshData()
                                                    }
                                                )
                                            }
                                            "Profile" -> {
                                                 ProfileScreen(
                                                     user = currentUser,
                                                     repository = repository,
                                                     isDarkMode = isDarkMode,
                                                     onDarkModeToggle = { enabled ->
                                                        repository.setDarkModeEnabled(enabled)
                                                        isDarkMode = enabled
                                                    },
                                                    onLogoutClick = {
                                                        showBars = false
                                                        currentScreen = AppScreen.LOGIN
                                                        CustomToastManager.showToast("Logged out successfully!")
                                                        coroutineScope.launch {
                                                            delay(400)
                                                            repository.clearData()
                                                            isLoggedIn = false
                                                            currentUser = repository.getUser()
                                                            selectedTab = "Home"
                                                            refreshData()
                                                        }
                                                    },
                                                    onProfileUpdated = { updatedUser ->
                                                        currentUser = updatedUser
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                AppScreen.JOB_DETAILS -> {
                                    JobDetailsScreen(
                                        jobId = selectedJobId,
                                        jobs = jobsList,
                                        onBackClick = navigateBack,
                                        onCallClick = { phone ->
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = "tel:$phone".toUri()
                                            }
                                            startActivity(intent)
                                        },
                                        onMapClick = { address ->
                                            val gmmIntentUri =
                                                ("geo:0,0?q=" + Uri.encode(address)).toUri()
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                            try {
                                                startActivity(mapIntent)
                                            } catch (_: Exception) {
                                                val webIntent = Intent(Intent.ACTION_VIEW,
                                                    ("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(
                                                        address
                                                    )).toUri())
                                                startActivity(webIntent)
                                            }
                                        },
                                        onAcceptClick = { jobId ->
                                            repository.updateJobStatus(jobId, JobStatus.ASSIGNED)
                                            refreshData()
                                            CustomToastManager.showToast("Job Accepted")
                                        },
                                        onRejectClick = { jobId ->
                                            repository.updateJobStatus(jobId, JobStatus.REJECTED)
                                            refreshData()
                                            CustomToastManager.showToast("Job Rejected")
                                        },
                                        onStartWorkClick = { jobId ->
                                            runWithLocation { lat, lng ->
                                                repository.updateJobStatus(jobId, JobStatus.IN_PROGRESS, lat, lng)
                                                refreshData()
                                                CustomToastManager.showToast("Work Started")
                                            }
                                        },
                                        onCreateReportClick = { _ ->
                                            currentScreen = AppScreen.SERVICE_REPORT
                                        },
                                        onMarkCompletedClick = { jobId ->
                                            repository.updateJobStatus(jobId, JobStatus.COMPLETED)
                                            refreshData()
                                            CustomToastManager.showToast("Job Marked as Completed")
                                        },
                                        onHistoryJobClick = { historyJobId ->
                                            selectedJobId = historyJobId
                                        }
                                    )
                                }
                                AppScreen.SERVICE_REPORT -> {
                                    ServiceReportFormScreen(
                                        jobId = selectedJobId,
                                        onBackClick = navigateBack,
                                        onSubmitSuccess = { findings, actions, remarks, evidence ->
                                            runWithLocation { lat, lng ->
                                                repository.submitServiceReport(selectedJobId, findings, actions, remarks, evidence, lat, lng)
                                                refreshData()
                                                currentScreen = AppScreen.JOB_DETAILS
                                                CustomToastManager.showToast("Service Report Submitted")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    TopDropToastOverlay()
                }
            }
        }
    }
}

    private fun getCurrentLocation(onResult: (Double?, Double?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onResult(null, null)
            return
        }
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                onResult(location.latitude, location.longitude)
            } else {
                onResult(null, null)
            }
        }.addOnFailureListener {
            onResult(null, null)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "FieldPro Notifications"
            val descriptionText = "Channel for FieldPro service requests updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("fieldpro_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSystemNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val jobId = extractJobId(message)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (jobId != null) {
                putExtra("jobId", jobId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "fieldpro_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}

fun extractJobId(description: String): String? {
    if (description.contains("assigned ")) {
        return description.substringAfter("assigned ").substringBefore(" for").trim()
    }
    if (description.contains("(") && description.contains(")")) {
        return description.substringAfter("(").substringBefore(")").trim()
    }
    return null
}

fun parseCreatedTimestamp(timestampStr: String?): Long {
    if (timestampStr.isNullOrEmpty()) return 0L
    return try {
        val sdf = java.text.SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a", java.util.Locale.getDefault())
        sdf.parse(timestampStr)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        TabItem("Home", R.drawable.ic_home),
        TabItem("Jobs", R.drawable.ic_jobs),
        TabItem("Alerts", R.drawable.ic_alerts),
        TabItem("Profile", R.drawable.ic_profile)
    )

    NavigationBar(
        containerColor = CardBg,
        tonalElevation = 8.dp,
        modifier = Modifier.background(CardBg)
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedTab == item.title,
                onClick = { onTabSelected(item.title) },
                icon = { 
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { 
                    Text(
                        text = item.title,
                        fontWeight = if (selectedTab == item.title) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = YellowText,
                    indicatorColor = YellowPrimary,
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                )
            )
        }
    }
}

data class TabItem(val title: String, val iconRes: Int)

object CustomToastManager {
    var toastMessage by mutableStateOf<String?>(null)
    var isError by mutableStateOf(false)
    private var dismissJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun showToast(message: String, isErrorToast: Boolean = false) {
        dismissJob?.cancel()
        toastMessage = message
        isError = isErrorToast
        dismissJob = scope.launch {
            delay(3000)
            if (toastMessage == message) {
                toastMessage = null
            }
        }
    }
}

@Composable
fun TopDropToastOverlay() {
    val message = CustomToastManager.toastMessage
    val isError = CustomToastManager.isError

    var activeMessage by remember { mutableStateOf<String?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message != null) {
            activeMessage = message
            isVisible = true
        } else {
            isVisible = false
            delay(400) // Wait for slide-up exit animation to complete
            activeMessage = null
        }
    }

    if (activeMessage != null) {
        Popup(
            alignment = Alignment.TopCenter,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError) Color(0xFFFEE2E2) else Color(0xFFFEF9C3)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isError) Color(0xFFFCA5A5) else Color(0xFFFEF08A)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isError) Color(0xFFDC2626) else Color(0xFFEAB308)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = activeMessage ?: "",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}