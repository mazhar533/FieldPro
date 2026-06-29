package com.mazhar.fieldpro.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.fieldpro.CustomToastManager
import com.mazhar.fieldpro.extractJobId
import com.mazhar.fieldpro.data.*
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory
import android.util.Base64
import com.mazhar.fieldpro.ui.theme.*
import java.util.*

import androidx.compose.ui.res.painterResource
import com.mazhar.fieldpro.R

sealed class AdminIcon {
    data class Vector(val imageVector: androidx.compose.ui.graphics.vector.ImageVector) : AdminIcon()
    data class DrawableRes(val resId: Int) : AdminIcon()
}

data class AdminTabItem(val title: String, val icon: AdminIcon)

sealed class AdminBottomSheetContent {
    data class TechJobs(val tech: User, val jobs: List<ServiceRequest>) : AdminBottomSheetContent()
    data class JobDetails(val job: ServiceRequest, val showBackButton: Boolean) : AdminBottomSheetContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: FieldProRepository,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onShowNotification: (String, String) -> Unit,
    adminOpenJobId: String = "",
    onAdminOpenJobIdConsumed: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Jobs") }
    val context = LocalContext.current

    // Lifiting allJobs & technicians & loading state to Dashboard Container Level
    var allJobs by remember { mutableStateOf<List<ServiceRequest>>(emptyList()) }
    var technicians by remember { mutableStateOf<List<User>>(emptyList()) }
    var previousStatuses by remember { mutableStateOf<Map<String, JobStatus>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var notificationsList by remember { mutableStateOf(repository.getNotifications()) }

    // Bottom sheet state
    var activeBottomSheetContent by remember { mutableStateOf<AdminBottomSheetContent?>(null) }

    LaunchedEffect(adminOpenJobId, allJobs) {
        if (adminOpenJobId.isNotEmpty() && allJobs.isNotEmpty()) {
            val job = allJobs.firstOrNull { it.id == adminOpenJobId }
            if (job != null) {
                activeBottomSheetContent = AdminBottomSheetContent.JobDetails(job, showBackButton = false)
                onAdminOpenJobIdConsumed()
            }
        }
    }

    DisposableEffect(Unit) {
        isLoading = true
        var jobsLoaded = false
        var techsLoaded = false
        
        val listenerReg = repository.getAllJobsForAdmin(
            onSuccess = { jobs ->
                val prev = previousStatuses
                if (prev != null) {
                    for (job in jobs) {
                        val oldStatus = prev[job.id]
                        if (oldStatus != null && oldStatus != job.status) {
                            val (title, icon) = when (job.status) {
                                JobStatus.ASSIGNED -> Pair("Job Accepted", NotificationIcon.BRIEFCASE)
                                JobStatus.IN_PROGRESS -> Pair("Job Started", NotificationIcon.CLOCK)
                                JobStatus.COMPLETED -> Pair("Job Completed", NotificationIcon.BRIEFCASE)
                                JobStatus.REJECTED -> Pair("Job Rejected", NotificationIcon.CLOCK)
                                else -> Pair("Job Updated", NotificationIcon.BRIEFCASE)
                            }
                            val message = when (job.status) {
                                JobStatus.ASSIGNED -> "Technician accepted job for ${job.customerName} (${job.id})"
                                JobStatus.IN_PROGRESS -> "Technician started work for ${job.customerName} (${job.id})"
                                JobStatus.COMPLETED -> "Technician completed job for ${job.customerName} (${job.id})"
                                JobStatus.REJECTED -> "Technician rejected job for ${job.customerName} (${job.id})"
                                else -> null
                            }
                            if (message != null) {
                                repository.addAdminNotification(job.id, title, message, icon)
                                onShowNotification(title, message)
                                notificationsList = repository.getNotifications()
                            }
                        }
                    }
                }
                previousStatuses = jobs.associate { it.id to it.status }
                allJobs = jobs.sortedByDescending { it.id.substringAfter("REQ-").toIntOrNull() ?: 0 }
                jobsLoaded = true
                if (techsLoaded) {
                    isLoading = false
                }
            },
            onFailure = {
                jobsLoaded = true
                if (techsLoaded) {
                    isLoading = false
                }
            }
        )
        repository.getAllTechnicians(
            onSuccess = { techs ->
                technicians = techs
                techsLoaded = true
                if (jobsLoaded) {
                    isLoading = false
                }
            },
            onFailure = {
                techsLoaded = true
                if (jobsLoaded) {
                    isLoading = false
                }
            }
        )
        onDispose {
            listenerReg.remove()
        }
    }

    Scaffold(
        topBar = {
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardBg,
                tonalElevation = 8.dp
            ) {
                listOf(
                    AdminTabItem("Jobs", AdminIcon.DrawableRes(R.drawable.ic_jobs)),
                    AdminTabItem("Create Job", AdminIcon.Vector(Icons.Default.Add)),
                    AdminTabItem("Alerts", AdminIcon.DrawableRes(R.drawable.ic_alerts)),
                    AdminTabItem("Analytics", AdminIcon.Vector(Icons.Default.Assessment)),
                    AdminTabItem("Profile", AdminIcon.DrawableRes(R.drawable.ic_profile))
                ).forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item.title,
                        onClick = { selectedTab = item.title },
                        icon = {
                            when (val iconType = item.icon) {
                                is AdminIcon.Vector -> Icon(iconType.imageVector, contentDescription = item.title, modifier = Modifier.size(24.dp))
                                is AdminIcon.DrawableRes -> Icon(painterResource(id = iconType.resId), contentDescription = item.title, modifier = Modifier.size(24.dp))
                            }
                        },
                        label = { Text(item.title, fontWeight = if (selectedTab == item.title) FontWeight.Bold else FontWeight.Normal) },
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
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "Jobs" -> AdminJobsTab(allJobs = allJobs, technicians = technicians, isLoading = isLoading, onJobClick = { job -> activeBottomSheetContent = AdminBottomSheetContent.JobDetails(job, showBackButton = false) })
                "Create Job" -> CreateJobTab(repository) { selectedTab = "Jobs" }
                "Alerts" -> AlertsScreen(
                    notifications = notificationsList,
                    onNotificationClick = { notifId ->
                        repository.markNotificationAsRead(notifId)
                        val notif = notificationsList.firstOrNull { it.id == notifId }
                        if (notif != null) {
                            val jobId = extractJobId(notif.description)
                            val job = allJobs.firstOrNull { it.id == jobId }
                            if (job != null) {
                                activeBottomSheetContent = AdminBottomSheetContent.JobDetails(job, showBackButton = false)
                            }
                        }
                        notificationsList = repository.getNotifications()
                    }
                )
                "Analytics" -> AdminAnalyticsTab(allJobs = allJobs, technicians = technicians, isLoading = isLoading, onTechClick = { tech, techJobs -> activeBottomSheetContent = AdminBottomSheetContent.TechJobs(tech, techJobs) })
                "Profile" -> AdminProfileTab(repository, isDarkMode, onDarkModeToggle, onLogoutClick)
            }
        }
    }

    if (activeBottomSheetContent != null) {
        ModalBottomSheet(
            onDismissRequest = { activeBottomSheetContent = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CardBg,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            tonalElevation = 8.dp,
            sheetGesturesEnabled = false
        ) {
            when (val content = activeBottomSheetContent) {
                is AdminBottomSheetContent.TechJobs -> {
                    BottomSheetTechJobsContent(
                        tech = content.tech,
                        jobs = content.jobs,
                        onJobClick = { job ->
                            activeBottomSheetContent = AdminBottomSheetContent.JobDetails(job, showBackButton = true)
                        },
                        onClose = { activeBottomSheetContent = null }
                    )
                }
                is AdminBottomSheetContent.JobDetails -> {
                    BottomSheetJobDetailsContent(
                        job = content.job,
                        technicians = technicians,
                        showBackButton = content.showBackButton,
                        onBackClick = {
                            if (content.job.assignedTechnician.isNotEmpty()) {
                                val tech = technicians.firstOrNull { it.email.equals(content.job.assignedTechnician, ignoreCase = true) }
                                if (tech != null) {
                                    val techJobs = allJobs.filter { it.assignedTechnician == tech.email }
                                    activeBottomSheetContent = AdminBottomSheetContent.TechJobs(tech, techJobs)
                                } else {
                                    activeBottomSheetContent = null
                                }
                            } else {
                                activeBottomSheetContent = null
                            }
                        },
                        onClose = { activeBottomSheetContent = null }
                    )
                }
                null -> {}
            }
        }
    }
}

@Composable
fun AdminJobsTab(
    allJobs: List<ServiceRequest>,
    technicians: List<User>,
    isLoading: Boolean,
    onJobClick: (ServiceRequest) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "All Service Jobs",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Monitor progress and assignments in real-time",
            fontSize = 14.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            AdminJobsShimmer()
        } else if (allJobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No jobs created yet.", color = TextMuted, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allJobs) { job ->
                    val techUser = technicians.firstOrNull { it.email.equals(job.assignedTechnician, ignoreCase = true) }
                    val techName = techUser?.fullName ?: if (job.assignedTechnician.isEmpty()) {
                        "Unassigned"
                    } else {
                        val name = job.assignedTechnician.split("@").firstOrNull() ?: ""
                        name.replace(".", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    }
                    AdminJobCard(job = job, techName = techName, onClick = { onJobClick(job) })
                }
            }
        }
    }
}

@Composable
fun AdminJobCard(job: ServiceRequest, techName: String, onClick: () -> Unit) {
    val statusBg = when (job.status) {
        JobStatus.PENDING -> RedLightBg
        JobStatus.ASSIGNED -> YellowLightBg
        JobStatus.IN_PROGRESS -> Color(0xFFFDF2E9)
        JobStatus.COMPLETED -> GreenLightBg
        JobStatus.REJECTED -> RedLightBg
    }
    val statusColor = when (job.status) {
        JobStatus.PENDING -> RedPending
        JobStatus.ASSIGNED -> YellowText
        JobStatus.IN_PROGRESS -> Color(0xFFE67E22)
        JobStatus.COMPLETED -> GreenCompleted
        JobStatus.REJECTED -> RedPending
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = job.status.name,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = job.customerName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextDark
            )

            Text(
                text = job.serviceType,
                color = TextMuted,
                fontSize = 14.sp
            )

            HorizontalDivider(color = CardBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (job.assignedTechnician.isEmpty()) "Unassigned" else "Tech: $techName",
                    color = if (job.assignedTechnician.isEmpty()) RedPending else YellowText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${job.serviceDate} • ${job.serviceTime}",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = TextDark) {
    Column {
        Text(text = label, fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 15.sp, color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobTab(repository: FieldProRepository, onJobCreated: () -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var customerName by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var issueDescription by remember { mutableStateOf("") }
    var serviceDate by remember { mutableStateOf("") }
    var serviceTime by remember { mutableStateOf("") }

    // Technicians Dropdown State
    var techniciansList by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedTech by remember { mutableStateOf<User?>(null) }
    var selectedTechName by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    // Fetch technicians list from Firestore
    LaunchedEffect(Unit) {
        repository.getAllTechnicians(
            onSuccess = { techs ->
                techniciansList = techs
            },
            onFailure = {
                CustomToastManager.showToast("Error fetching technicians", isErrorToast = true)
            }
        )
    }

    // Date Picker Dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            serviceDate = "${month + 1}/$dayOfMonth/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Time Picker Dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formatHour = if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay
            val amPm = if (hourOfDay >= 12) "PM" else "AM"
            val formatMinute = String.format("%02d", minute)
            serviceTime = String.format("%02d:%s %s", formatHour, formatMinute, amPm)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Create Service Job",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Fill in job details and assign to a technician",
            fontSize = 14.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Customer Name
        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            placeholder = { Text("e.g. John Doe") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YellowPrimary,
                unfocusedBorderColor = CardBorder
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Service Type
        OutlinedTextField(
            value = serviceType,
            onValueChange = { serviceType = it },
            label = { Text("Service Type") },
            placeholder = { Text("e.g. Fiber Internet Installation") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YellowPrimary,
                unfocusedBorderColor = CardBorder
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Issue Description
        OutlinedTextField(
            value = issueDescription,
            onValueChange = { issueDescription = it },
            label = { Text("Issue Description") },
            placeholder = { Text("Describe the issues or requests...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YellowPrimary,
                unfocusedBorderColor = CardBorder
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Service Location
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Service Location") },
            placeholder = { Text("e.g. 123 Main St, New York") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YellowPrimary,
                unfocusedBorderColor = CardBorder
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contact Number
        OutlinedTextField(
            value = contactNumber,
            onValueChange = { contactNumber = it },
            label = { Text("Contact Number") },
            placeholder = { Text("e.g. +1 555-0199") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YellowPrimary,
                unfocusedBorderColor = CardBorder
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date and Time Fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { datePickerDialog.show() }
            ) {
                OutlinedTextField(
                    value = serviceDate,
                    onValueChange = {},
                    label = { Text("Service Date") },
                    placeholder = { Text("MM/DD/YYYY") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = CardBorder,
                        disabledTextColor = TextDark,
                        disabledLabelColor = TextMuted
                    )
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { timePickerDialog.show() }
            ) {
                OutlinedTextField(
                    value = serviceTime,
                    onValueChange = {},
                    label = { Text("Service Time") },
                    placeholder = { Text("HH:MM AM/PM") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = CardBorder,
                        disabledTextColor = TextDark,
                        disabledLabelColor = TextMuted
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technician Assignment Dropdown
        Text(
            text = "Assign to Technician",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedTechName.ifEmpty { "Select a Technician" },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YellowPrimary,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { dropdownExpanded = true }
            )

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(CardBg)
            ) {
                if (techniciansList.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No technicians registered yet", color = TextMuted) },
                        onClick = { dropdownExpanded = false }
                    )
                } else {
                    techniciansList.forEach { tech ->
                        DropdownMenuItem(
                            text = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(text = tech.fullName, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text(text = "${tech.email} | 📞 ${tech.contactNumber}", fontSize = 11.sp, color = TextMuted)
                                    if (tech.expertise.isNotEmpty()) {
                                        Text(text = "Expertise: ${tech.expertise}", fontSize = 11.sp, color = YellowText, fontWeight = FontWeight.Medium)
                                    }
                                }
                            },
                            onClick = {
                                selectedTech = tech
                                selectedTechName = if (tech.expertise.isNotEmpty()) {
                                    "${tech.fullName} (${tech.expertise})"
                                } else {
                                    tech.fullName
                                }
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Submit Button
        Button(
            onClick = {
                if (customerName.isBlank() || serviceType.isBlank() || location.isBlank() ||
                    contactNumber.isBlank() || serviceDate.isBlank() || serviceTime.isBlank() ||
                    selectedTech == null
                ) {
                    CustomToastManager.showToast("Please fill in all fields and assign a technician.", isErrorToast = true)
                } else {
                    isLoading = true
                    val randomId = "REQ-${1000 + Random().nextInt(9000)}"
                    val newJob = ServiceRequest(
                        id = randomId,
                        customerName = customerName,
                        serviceType = serviceType,
                        issueDescription = issueDescription,
                        status = JobStatus.PENDING,
                        assignedTechnician = selectedTech!!.email,
                        serviceDate = serviceDate,
                        serviceTime = serviceTime,
                        contactNumber = contactNumber,
                        location = location
                    )
                    repository.createJob(
                        job = newJob,
                        onSuccess = {
                            isLoading = false
                            CustomToastManager.showToast("Job successfully created and assigned!")
                            onJobCreated()
                        },
                        onFailure = { err ->
                            isLoading = false
                            CustomToastManager.showToast("Error: $err", isErrorToast = true)
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Publish & Assign Job", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AdminProfileTab(
    repository: FieldProRepository,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onLogoutClick: () -> Unit
) {
    val currentUser = repository.getUser()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Admin Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = YellowLightBg),
            border = BorderStroke(1.dp, YellowLightBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = TextDarkColor,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentUser.fullName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ADMINISTRATOR",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = currentUser.email,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDarkColor
                    )
                }
            }
        }

        // Dark Mode Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BackgroundLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Dark Mode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = YellowPrimary)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Log Out Button
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RedLightBg,
                contentColor = RedPending
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Log Out"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}



@Composable
fun AdminJobsShimmer() {
    val brush = shimmerBrush()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(width = 80.dp, height = 16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        Box(modifier = Modifier.size(width = 90.dp, height = 24.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                    }
                    Box(modifier = Modifier.size(width = 150.dp, height = 20.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Box(modifier = Modifier.size(width = 100.dp, height = 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    HorizontalDivider(color = CardBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(width = 120.dp, height = 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        Box(modifier = Modifier.size(width = 100.dp, height = 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsScreenShimmer() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        ),
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Performance Analytics",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Real-time workforce performance indicators",
                fontSize = 14.sp,
                color = TextMuted
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.size(width = 100.dp, height = 12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Box(modifier = Modifier.size(width = 60.dp, height = 28.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(brush))
            }
        }

        Box(
            modifier = Modifier
                .size(width = 160.dp, height = 24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(brush)
        )

        repeat(2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(brush)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(width = 140.dp, height = 16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        Box(modifier = Modifier.size(width = 180.dp, height = 12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(width = 50.dp, height = 10.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                            Box(modifier = Modifier.size(width = 60.dp, height = 10.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(width = 40.dp, height = 20.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        Box(modifier = Modifier.size(width = 30.dp, height = 10.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnalyticsTab(
    allJobs: List<ServiceRequest>,
    technicians: List<User>,
    isLoading: Boolean,
    onTechClick: (User, List<ServiceRequest>) -> Unit
) {
    if (isLoading) {
        AnalyticsScreenShimmer()
    } else {
        val totalJobs = allJobs.size
        val completedJobs = allJobs.count { it.status == JobStatus.COMPLETED }
        val activeJobs = allJobs.count { it.status == JobStatus.IN_PROGRESS }
        val assignedJobs = allJobs.count { it.status == JobStatus.ASSIGNED }
        val pendingJobs = allJobs.count { it.status == JobStatus.PENDING }
        val rejectedJobs = allJobs.count { it.status == JobStatus.REJECTED }
        
        val completionRate = if (totalJobs > 0) (completedJobs.toFloat() / totalJobs.toFloat() * 100).toInt() else 0

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Performance Analytics",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Real-time workforce performance indicators",
                    fontSize = 14.sp,
                    color = TextMuted
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "Completion Rate", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$completionRate%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = YellowPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { completionRate.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = YellowPrimary,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Technician Rankings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            items(technicians) { tech ->
                val techJobs = allJobs.filter { it.assignedTechnician == tech.email }
                val techTotal = techJobs.size
                val techCompleted = techJobs.count { it.status == JobStatus.COMPLETED }
                val techActive = techJobs.count { it.status == JobStatus.IN_PROGRESS }
                val techAssigned = techJobs.count { it.status == JobStatus.ASSIGNED }
                val techPending = techJobs.count { it.status == JobStatus.PENDING }
                val techRejected = techJobs.count { it.status == JobStatus.REJECTED }
                val techRate = if (techTotal > 0) (techCompleted.toFloat() / techTotal.toFloat() * 100).toInt() else 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTechClick(tech, techJobs) },
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(YellowLightBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tech.fullName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").take(2),
                                color = YellowPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = tech.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                            Text(text = "${tech.email} | ${tech.contactNumber}", fontSize = 12.sp, color = TextMuted)
                            if (tech.expertise.isNotEmpty()) {
                                Text(text = "Expertise: ${tech.expertise}", fontSize = 12.sp, color = YellowText, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "Tot: $techTotal", fontSize = 10.sp, color = TextMuted)
                                Text(text = "Comp: $techCompleted", fontSize = 10.sp, color = GreenCompleted, fontWeight = FontWeight.Bold)
                                Text(text = "Act: $techActive", fontSize = 10.sp, color = Color(0xFFE67E22), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "Assg: $techAssigned", fontSize = 10.sp, color = YellowText, fontWeight = FontWeight.Bold)
                                Text(text = "Pend: $techPending", fontSize = 10.sp, color = RedPending, fontWeight = FontWeight.Bold)
                                Text(text = "Rej: $techRejected", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "$techRate%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = YellowPrimary)
                            Text(text = "Success", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSheetTechJobsContent(
    tech: User,
    jobs: List<ServiceRequest>,
    onJobClick: (ServiceRequest) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = tech.fullName, fontWeight = FontWeight.Black, fontSize = 22.sp, color = TextDark)
                Text(text = "${tech.email} | ${tech.contactNumber}", fontSize = 13.sp, color = TextMuted)
                if (tech.expertise.isNotEmpty()) {
                    Text(text = "Expertise: ${tech.expertise}", fontSize = 13.sp, color = YellowText, fontWeight = FontWeight.SemiBold)
                }
                Text(text = "${jobs.size} Jobs Assigned", fontSize = 13.sp, color = TextMuted)
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        if (jobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Text(text = "No jobs assigned to this technician.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(jobs) { job ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onJobClick(job) },
                        colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = job.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                                Text(text = job.serviceType, fontSize = 12.sp, color = TextMuted)
                                Text(text = "${job.serviceDate} at ${job.serviceTime}", fontSize = 11.sp, color = TextMuted)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (job.status) {
                                            JobStatus.PENDING -> RedLightBg
                                            JobStatus.ASSIGNED -> YellowLightBg
                                            JobStatus.IN_PROGRESS -> Color(0xFFFDF2E9)
                                            JobStatus.COMPLETED -> GreenLightBg
                                            JobStatus.REJECTED -> RedLightBg
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = job.status.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (job.status) {
                                        JobStatus.PENDING -> RedPending
                                        JobStatus.ASSIGNED -> YellowText
                                        JobStatus.IN_PROGRESS -> Color(0xFFE67E22)
                                        JobStatus.COMPLETED -> GreenCompleted
                                        JobStatus.REJECTED -> RedPending
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetJobDetailsContent(
    job: ServiceRequest,
    technicians: List<User>,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onClose: () -> Unit
) {
    val localContext = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBackButton) {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = "Job Details: ${job.id}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        Column(
            modifier = Modifier
                .heightIn(max = 450.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailRow(label = "Customer Name", value = job.customerName)
            DetailRow(label = "Service Type", value = job.serviceType)
            DetailRow(label = "Issue Description", value = job.issueDescription)
            DetailRow(label = "Location", value = job.location)
            DetailRow(label = "Contact Number", value = job.contactNumber)
            DetailRow(label = "Scheduled On", value = "${job.serviceDate} at ${job.serviceTime}")
            DetailRow(
                label = "Status",
                value = job.status.name,
                valueColor = when (job.status) {
                    JobStatus.PENDING -> RedPending
                    JobStatus.ASSIGNED -> YellowText
                    JobStatus.IN_PROGRESS -> Color(0xFFE67E22)
                    JobStatus.COMPLETED -> GreenCompleted
                    JobStatus.REJECTED -> RedPending
                }
            )
            val techUser = technicians.firstOrNull { it.email.equals(job.assignedTechnician, ignoreCase = true) }
            val techDisplay = if (job.assignedTechnician.isEmpty()) {
                "Unassigned"
            } else {
                val name = techUser?.fullName ?: run {
                    val prefix = job.assignedTechnician.split("@").firstOrNull() ?: ""
                    prefix.replace(".", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                }
                "$name (${job.assignedTechnician})"
            }
            DetailRow(label = "Assigned To", value = techDisplay)
            
            if (job.status == JobStatus.COMPLETED) {
                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 4.dp))
                Text(text = "Service Report Details", fontWeight = FontWeight.Bold, color = TextDark)
                DetailRow(label = "Report Timestamp", value = job.reportTimestamp ?: "")
                DetailRow(label = "Findings", value = job.findings ?: "")
                DetailRow(label = "Actions Taken", value = job.actionsTaken ?: "")
                DetailRow(label = "Completion Remarks", value = job.completionRemarks ?: "")

                if (!job.evidenceImageBase64.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Service Evidence Photo", fontWeight = FontWeight.Bold, color = TextDark)
                    val bitmap = remember(job.evidenceImageBase64) {
                        try {
                            val decodedString = Base64.decode(job.evidenceImageBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Evidence Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (job.startLatitude != null || job.completionLatitude != null) {
                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 4.dp))
                Text(text = "GPS Location Verification", fontWeight = FontWeight.Bold, color = TextDark)
                
                if (job.startLatitude != null && job.startLongitude != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Work Start Location", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("Lat: ${job.startLatitude}, Lng: ${job.startLongitude}", fontSize = 13.sp, color = TextDark)
                        }
                        Button(
                            onClick = {
                                val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=${job.startLatitude},${job.startLongitude}")
                                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                localContext.startActivity(mapIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YellowLightBg, contentColor = YellowPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (job.completionLatitude != null && job.completionLongitude != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Report Submission Location", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("Lat: ${job.completionLatitude}, Lng: ${job.completionLongitude}", fontSize = 13.sp, color = TextDark)
                        }
                        Button(
                            onClick = {
                                val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=${job.completionLatitude},${job.completionLongitude}")
                                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                localContext.startActivity(mapIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YellowLightBg, contentColor = YellowPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
