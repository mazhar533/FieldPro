package com.mazhar.fieldpro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mazhar.fieldpro.data.*
import com.mazhar.fieldpro.ui.screens.*
import com.mazhar.fieldpro.ui.theme.BackgroundLight
import com.mazhar.fieldpro.ui.theme.BluePrimary
import com.mazhar.fieldpro.ui.theme.FieldProTheme
import androidx.core.net.toUri

enum class AppScreen {
    LOGIN,
    MAIN,
    JOB_DETAILS,
    SERVICE_REPORT
}

class MainActivity : ComponentActivity() {
    private lateinit var repository: FieldProRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = FieldProRepository(this)
        enableEdgeToEdge()

        setContent {
            FieldProTheme {
                var isLoggedIn by remember { mutableStateOf(repository.isUserLoggedIn()) }
                var currentScreen by remember { mutableStateOf(if (isLoggedIn) AppScreen.MAIN else AppScreen.LOGIN) }
                var selectedTab by remember { mutableStateOf("Home") }
                var selectedJobId by remember { mutableStateOf("") }

                // Live list states
                var jobsList by remember { mutableStateOf(repository.getJobs()) }
                var notificationsList by remember { mutableStateOf(repository.getNotifications()) }
                val currentUser by remember { mutableStateOf(repository.getUser()) }

                // Refresh helper
                val refreshData = {
                    jobsList = repository.getJobs()
                    notificationsList = repository.getNotifications()
                }

                // Handle back pressed or custom back navigation
                val navigateBack: () -> Unit = {
                    if (currentScreen == AppScreen.JOB_DETAILS) {
                        currentScreen = AppScreen.MAIN
                    } else if (currentScreen == AppScreen.SERVICE_REPORT) {
                        currentScreen = AppScreen.JOB_DETAILS
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentScreen == AppScreen.MAIN) {
                            BottomNavigationBar(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                        }
                    },
                    containerColor = BackgroundLight
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
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
                                AppScreen.LOGIN -> {
                                    LoginScreen(
                                        onLoginSuccess = { email ->
                                            repository.setUserLoggedIn(true)
                                            isLoggedIn = true
                                            currentScreen = AppScreen.MAIN
                                            Toast.makeText(this@MainActivity, "Signed in as $email", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                AppScreen.MAIN -> {
                                    when (selectedTab) {
                                        "Home" -> {
                                            HomeScreen(
                                                userName = currentUser.fullName.split(" ").firstOrNull() ?: "Alex",
                                                jobs = jobsList,
                                                onViewAllJobsClick = { selectedTab = "Jobs" },
                                                onJobClick = { jobId ->
                                                    selectedJobId = jobId
                                                    currentScreen = AppScreen.JOB_DETAILS
                                                }
                                            )
                                        }
                                        "Jobs" -> {
                                            JobsScreen(
                                                jobs = jobsList,
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
                                                    refreshData()
                                                }
                                            )
                                        }
                                        "Profile" -> {
                                            ProfileScreen(
                                                user = currentUser,
                                                onLogoutClick = {
                                                    repository.clearData()
                                                    isLoggedIn = false
                                                    selectedTab = "Home"
                                                    refreshData()
                                                    currentScreen = AppScreen.LOGIN
                                                }
                                            )
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
                                            Toast.makeText(this@MainActivity, "Job Accepted", Toast.LENGTH_SHORT).show()
                                        },
                                        onRejectClick = { jobId ->
                                            repository.updateJobStatus(jobId, JobStatus.PENDING) // Mock reset or reject
                                            refreshData()
                                            Toast.makeText(this@MainActivity, "Job Rejected", Toast.LENGTH_SHORT).show()
                                        },
                                        onStartWorkClick = { jobId ->
                                            repository.updateJobStatus(jobId, JobStatus.IN_PROGRESS)
                                            refreshData()
                                            Toast.makeText(this@MainActivity, "Work Started", Toast.LENGTH_SHORT).show()
                                        },
                                        onCreateReportClick = { _ ->
                                            currentScreen = AppScreen.SERVICE_REPORT
                                        },
                                        onMarkCompletedClick = { jobId ->
                                            repository.updateJobStatus(jobId, JobStatus.COMPLETED)
                                            refreshData()
                                            Toast.makeText(this@MainActivity, "Job Marked as Completed", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                AppScreen.SERVICE_REPORT -> {
                                    ServiceReportFormScreen(
                                        jobId = selectedJobId,
                                        onBackClick = navigateBack,
                                        onSubmitSuccess = { findings, actions, remarks ->
                                            repository.submitServiceReport(selectedJobId, findings, actions, remarks)
                                            refreshData()
                                            currentScreen = AppScreen.JOB_DETAILS
                                            Toast.makeText(this@MainActivity, "Service Report Submitted", Toast.LENGTH_LONG).show()
                                        }
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

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        TabItem("Home", Icons.Default.Home),
        TabItem("Jobs", Icons.Default.DateRange),
        TabItem("Alerts", Icons.Default.Notifications),
        TabItem("Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.background(Color.White)
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedTab == item.title,
                onClick = { onTabSelected(item.title) },
                icon = { 
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { 
                    Text(
                        text = item.title,
                        fontWeight = if (selectedTab == item.title) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BluePrimary,
                    selectedTextColor = BluePrimary,
                    indicatorColor = Color(0xFFD2E3FC),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                )
            )
        }
    }
}

data class TabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)