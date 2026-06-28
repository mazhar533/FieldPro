package com.mazhar.fieldpro.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.fieldpro.data.*
import com.mazhar.fieldpro.ui.theme.*
import java.util.*

data class AdminTabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: FieldProRepository,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onLogoutClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Jobs") }
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CardBg,
                tonalElevation = 8.dp
            ) {
                listOf(
                    AdminTabItem("Jobs", Icons.Default.Home),
                    AdminTabItem("Create Job", Icons.Default.Add),
                    AdminTabItem("Profile", Icons.Default.Person)
                ).forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item.title,
                        onClick = { selectedTab = item.title },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontWeight = if (selectedTab == item.title) FontWeight.Bold else FontWeight.Normal) },
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
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "Jobs" -> AdminJobsTab(repository)
                "Create Job" -> CreateJobTab(repository) { selectedTab = "Jobs" }
                "Profile" -> AdminProfileTab(repository, isDarkMode, onDarkModeToggle, onLogoutClick)
            }
        }
    }
}

@Composable
fun AdminJobsTab(repository: FieldProRepository) {
    var allJobs by remember { mutableStateOf<List<ServiceRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedJobForDetails by remember { mutableStateOf<ServiceRequest?>(null) }

    LaunchedEffect(Unit) {
        repository.getAllJobsForAdmin(
            onSuccess = { jobs ->
                allJobs = jobs
                isLoading = false
            },
            onFailure = {
                isLoading = false
            }
        )
    }

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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
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
                    AdminJobCard(job = job, onClick = { selectedJobForDetails = job })
                }
            }
        }
    }

    if (selectedJobForDetails != null) {
        val job = selectedJobForDetails!!
        AlertDialog(
            onDismissRequest = { selectedJobForDetails = null },
            title = { Text(text = "Job Details: ${job.id}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
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
                            JobStatus.ASSIGNED -> BlueText
                            JobStatus.IN_PROGRESS -> Color(0xFFE67E22)
                            JobStatus.COMPLETED -> GreenCompleted
                        }
                    )
                    DetailRow(label = "Assigned To", value = job.assignedTechnician)
                    
                    if (job.status == JobStatus.COMPLETED) {
                        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Text(text = "Service Report Details", fontWeight = FontWeight.Bold, color = TextDark)
                        DetailRow(label = "Report Timestamp", value = job.reportTimestamp ?: "")
                        DetailRow(label = "Findings", value = job.findings ?: "")
                        DetailRow(label = "Actions Taken", value = job.actionsTaken ?: "")
                        DetailRow(label = "Completion Remarks", value = job.completionRemarks ?: "")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedJobForDetails = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun AdminJobCard(job: ServiceRequest, onClick: () -> Unit) {
    val statusBg = when (job.status) {
        JobStatus.PENDING -> RedLightBg
        JobStatus.ASSIGNED -> BlueLightBg
        JobStatus.IN_PROGRESS -> Color(0xFFFDF2E9)
        JobStatus.COMPLETED -> GreenLightBg
    }
    val statusColor = when (job.status) {
        JobStatus.PENDING -> RedPending
        JobStatus.ASSIGNED -> BlueText
        JobStatus.IN_PROGRESS -> Color(0xFFE67E22)
        JobStatus.COMPLETED -> GreenCompleted
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
                    text = "Tech: ${job.assignedTechnician.split("@").firstOrNull() ?: ""}",
                    color = BlueText,
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
                Toast.makeText(context, "Error fetching technicians", Toast.LENGTH_SHORT).show()
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
                focusedBorderColor = BluePrimary,
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
                focusedBorderColor = BluePrimary,
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
                focusedBorderColor = BluePrimary,
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
                focusedBorderColor = BluePrimary,
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
                focusedBorderColor = BluePrimary,
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
                    focusedBorderColor = BluePrimary,
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
                            text = { Text("${tech.fullName} (${tech.email})") },
                            onClick = {
                                selectedTech = tech
                                selectedTechName = tech.fullName
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
                    Toast.makeText(context, "Please fill in all fields and assign a technician.", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "Job successfully created and assigned!", Toast.LENGTH_LONG).show()
                            onJobCreated()
                        },
                        onFailure = { err ->
                            isLoading = false
                            Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
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
            colors = CardDefaults.cardColors(containerColor = BlueLightBg),
            border = BorderStroke(1.dp, BlueLightBg)
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
                        color = BlueText
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
                    colors = SwitchDefaults.colors(checkedThumbColor = BluePrimary)
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
