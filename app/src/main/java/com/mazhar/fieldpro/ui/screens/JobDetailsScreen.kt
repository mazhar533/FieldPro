package com.mazhar.fieldpro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.fieldpro.data.JobStatus
import com.mazhar.fieldpro.data.ServiceRequest
import com.mazhar.fieldpro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    jobId: String,
    jobs: List<ServiceRequest>,
    onBackClick: () -> Unit,
    onCallClick: (String) -> Unit,
    onMapClick: (String) -> Unit,
    onAcceptClick: (String) -> Unit,
    onRejectClick: (String) -> Unit,
    onStartWorkClick: (String) -> Unit,
    onCreateReportClick: (String) -> Unit,
    onMarkCompletedClick: (String) -> Unit
) {
    val job = jobs.firstOrNull { it.id == jobId }

    if (job == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Job details not found.", color = TextMuted)
        }
        return
    }

    // Color code status badge
    val (badgeBg, badgeText, statusLabel) = when (job.status) {
        JobStatus.PENDING -> Triple(RedLightBg, RedPending, "PENDING")
        JobStatus.ASSIGNED -> Triple(BlueLightBg, BlueText, "ASSIGNED")
        JobStatus.IN_PROGRESS -> Triple(PurpleLightBg, PurplePrimary, "IN PROGRESS")
        JobStatus.COMPLETED -> Triple(GreenLightBg, GreenCompleted, "COMPLETED")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Job Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Card (Light Blue Banner)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BlueLightBg),
                border = BorderStroke(1.dp, BlueLightBg)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = job.id,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueText
                        )
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeBg)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeText
                            )
                        }
                    }

                    Text(
                        text = job.serviceType,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        text = "${job.serviceDate}, ${job.serviceTime}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }

            // Customer Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Customer Info",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    // Name Row
                    Column {
                        Text(
                            text = "NAME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = job.customerName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    }

                    HorizontalDivider(color = CardBorder, thickness = 1.dp)

                    // Contact Row with Call button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CONTACT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = job.contactNumber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GreenLightBg)
                                .clickable { onCallClick(job.contactNumber) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Customer",
                                tint = GreenCompleted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = CardBorder, thickness = 1.dp)

                    // Location Row with Map button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LOCATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = job.location,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BlueLightBg)
                                .clickable { onMapClick(job.location) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Locate on Map",
                                tint = BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Issue Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Issue Description",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    
                    Text(
                        text = job.issueDescription,
                        fontSize = 15.sp,
                        color = TextDark,
                        lineHeight = 22.sp
                    )
                }
            }

            // Completed Service Report info (If Completed)
            if (job.status == JobStatus.COMPLETED && job.findings != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Service Report Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        
                        Column {
                            Text("FINDINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(job.findings ?: "", fontSize = 14.sp, color = TextDark)
                        }
                        
                        Column {
                            Text("ACTIONS PERFORMED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(job.actionsTaken ?: "", fontSize = 14.sp, color = TextDark)
                        }
                        
                        Column {
                            Text("COMPLETION REMARKS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(job.completionRemarks ?: "", fontSize = 14.sp, color = TextDark)
                        }

                        if (job.reportTimestamp != null) {
                            Text(
                                text = "Submitted on ${job.reportTimestamp}",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Job Timeline Section (For tracking work flow)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Job Timeline",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    
                    val formatTimestamp: (String?, String, String) -> Pair<String, String> = { timestamp, fallbackDate, fallbackTime ->
                        if (timestamp.isNullOrEmpty()) {
                            Pair(fallbackDate, fallbackTime)
                        } else {
                            val parts = timestamp.split(",")
                            val date = parts.firstOrNull()?.trim() ?: fallbackDate
                            val rawTime = parts.lastOrNull()?.trim() ?: fallbackTime
                            
                            val timeParts = rawTime.split(" ")
                            val timeNum = timeParts.firstOrNull()?.split(":") ?: emptyList()
                            val amPm = timeParts.lastOrNull() ?: ""
                            val cleanTime = if (timeNum.size >= 2) {
                                "${timeNum[0]}:${timeNum[1]} $amPm"
                            } else {
                                rawTime
                            }
                            Pair(date, cleanTime)
                        }
                    }

                    val (pendingDate, pendingTime) = formatTimestamp(job.createdTimestamp, job.serviceDate, job.serviceTime)
                    val (assignedDate, assignedTime) = formatTimestamp(job.assignedTimestamp, "--/--/----", "--:--")
                    val (inProgressDate, inProgressTime) = formatTimestamp(job.inProgressTimestamp, "--/--/----", "--:--")
                    val (completedDate, completedTime) = formatTimestamp(job.reportTimestamp, "--/--/----", "--:--")

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TimelineItem(
                            status = "Pending",
                            date = pendingDate,
                            time = pendingTime,
                            isCompleted = true,
                            isLast = false
                        )
                        TimelineItem(
                            status = "Assigned",
                            date = assignedDate,
                            time = assignedTime,
                            isCompleted = !job.assignedTimestamp.isNullOrEmpty() || job.status != JobStatus.PENDING,
                            isLast = false
                        )
                        TimelineItem(
                            status = "In Progress",
                            date = inProgressDate,
                            time = inProgressTime,
                            isCompleted = !job.inProgressTimestamp.isNullOrEmpty() || job.status == JobStatus.COMPLETED,
                            isLast = false
                        )
                        TimelineItem(
                            status = "Completed",
                            date = completedDate,
                            time = completedTime,
                            isCompleted = job.status == JobStatus.COMPLETED,
                            isLast = true
                        )
                    }
                }
            }

            // Dynamic Context-Aware Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (job.status) {
                    JobStatus.PENDING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onAcceptClick(job.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                            ) {
                                Text("Accept Job", fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = { onRejectClick(job.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RedLightBg, contentColor = RedPending)
                            ) {
                                Text("Reject", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    JobStatus.ASSIGNED -> {
                        Button(
                            onClick = { onStartWorkClick(job.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Work", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    JobStatus.IN_PROGRESS -> {
                        Button(
                            onClick = { onCreateReportClick(job.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueLightBg, contentColor = BlueText)
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Service Report", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onMarkCompletedClick(job.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenCompleted)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark Completed", fontWeight = FontWeight.Bold)
                        }
                    }
                    JobStatus.COMPLETED -> {
                        // Completed notice
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GreenLightBg)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = GreenCompleted
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Job is completed successfully",
                                    fontWeight = FontWeight.Bold,
                                    color = GreenCompleted
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
fun TimelineItem(
    status: String,
    date: String,
    time: String,
    isCompleted: Boolean,
    isLast: Boolean
) {
    val lineCol = if (isCompleted) BluePrimary else CardBorder
    val dotCol = if (isCompleted) BluePrimary else CardBorder

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vertical Line & Dot Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(dotCol)
            )
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(lineCol)
                )
            }
        }

        // Detail Content Column
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status,
                fontSize = 15.sp,
                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCompleted) TextDark else TextMuted
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = date,
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Text(
                    text = time,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) TextDark else TextMuted
                )
            }
        }
    }
}
