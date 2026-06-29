package com.mazhar.fieldpro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.fieldpro.data.JobStatus
import com.mazhar.fieldpro.data.ServiceRequest
import com.mazhar.fieldpro.ui.theme.*

@Composable
fun JobsScreen(
    jobs: List<ServiceRequest>,
    initialTab: String = "Active",
    isLoading: Boolean = false,
    onJobClick: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }
    val tabs = listOf("All", "Active", "Pending", "Completed")

    val filteredJobs = remember(jobs, selectedTab) {
        when (selectedTab) {
            "All" -> jobs
            "Active" -> jobs.filter { it.status == JobStatus.ASSIGNED || it.status == JobStatus.IN_PROGRESS }
            "Pending" -> jobs.filter { it.status == JobStatus.PENDING }
            else -> jobs.filter { it.status == JobStatus.COMPLETED }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Jobs",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Pill Tab Row
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab
                val backgroundColor = if (isSelected) YellowPrimary else Color.Transparent
                val textColor = if (isSelected) Color.White else TextDark
                val borderStroke = if (isSelected) null else BorderStroke(1.dp, CardBorder)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(backgroundColor)
                        .clickable { selectedTab = tab }
                        .then(
                            if (borderStroke != null) Modifier.background(CardBg) else Modifier
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            JobsScreenShimmer()
        } else {
            // Animated Lists of jobs
            AnimatedContent(
                targetState = filteredJobs,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                },
                label = "JobsListAnimation"
            ) { targetJobs ->
                if (targetJobs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No jobs found in this section.",
                            color = TextMuted,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(targetJobs, key = { _, job -> job.id }) { index, job ->
                            StaggeredSlideUpItem(index = index) {
                                JobCard(job = job, onClick = { onJobClick(job.id) })
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JobCard(
    job: ServiceRequest,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "JobCardPress"
    )

    // Color code status badge
    val (badgeBg, badgeText, statusLabel) = when (job.status) {
        JobStatus.PENDING -> Triple(RedLightBg, RedPending, "PENDING")
        JobStatus.ASSIGNED -> Triple(YellowLightBg, YellowText, "ASSIGNED")
        JobStatus.IN_PROGRESS -> Triple(PurpleLightBg, PurplePrimary, "IN PROGRESS")
        JobStatus.COMPLETED -> Triple(GreenLightBg, GreenCompleted, "COMPLETED")
        JobStatus.REJECTED -> Triple(RedLightBg, RedPending, "REJECTED")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = job.customerName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = job.id,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }

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
                text = job.issueDescription,
                fontSize = 14.sp,
                color = TextDark,
                lineHeight = 20.sp
            )

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = YellowText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = job.location,
                    fontSize = 14.sp,
                    color = TextMuted
                )
            }

            // Date / Time Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = job.serviceDate,
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Time",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = job.serviceTime,
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun StaggeredSlideUpItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { 100 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(300)),
        exit = fadeOut()
    ) {
        content()
    }
}

@Composable
fun JobsScreenShimmer() {
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
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(width = 80.dp, height = 24.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                        Box(modifier = Modifier.size(width = 120.dp, height = 16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    }
                    Box(modifier = Modifier.size(width = 200.dp, height = 20.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Box(modifier = Modifier.size(width = 150.dp, height = 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    HorizontalDivider(color = CardBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(width = 100.dp, height = 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        Box(modifier = Modifier.size(width = 120.dp, height = 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    }
                }
            }
        }
    }
}

