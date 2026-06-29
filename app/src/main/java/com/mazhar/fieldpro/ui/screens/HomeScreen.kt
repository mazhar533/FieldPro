package com.mazhar.fieldpro.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.style.TextOverflow
import com.mazhar.fieldpro.data.JobStatus
import com.mazhar.fieldpro.data.ServiceRequest
import com.mazhar.fieldpro.ui.theme.*

@Composable
fun HomeScreen(
    userName: String,
    jobs: List<ServiceRequest>,
    isLoading: Boolean = false,
    onViewAllJobsClick: (String) -> Unit,
    onJobClick: (String) -> Unit
) {
    if (isLoading) {
        HomeScreenShimmer(userName)
        return
    }

    val activeCount = jobs.count { it.status == JobStatus.ASSIGNED || it.status == JobStatus.IN_PROGRESS }
    val completedCount = jobs.count { it.status == JobStatus.COMPLETED }
    val pendingCount = jobs.count { it.status == JobStatus.PENDING }

    val upNextJob = jobs.firstOrNull { it.status == JobStatus.ASSIGNED || it.status == JobStatus.IN_PROGRESS } 
        ?: jobs.firstOrNull { it.status == JobStatus.PENDING }

    // Staggered loading state animations
    var animateCards by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateCards = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Greeting Header
            Text(
                text = "Hello, $userName",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Here is your daily overview",
                fontSize = 16.sp,
                color = TextMuted
            )
        }

        // Stats Grid Card Block
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Active Card (animated scale & slide)
                val leftCardOffset by animateDpAsState(
                    targetValue = if (animateCards) 0.dp else 100.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "LeftCardOffset"
                )
                
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .graphicsLayer(translationX = leftCardOffset.value)
                        .clip(RoundedCornerShape(24.dp))
                        .background(YellowLightBg)
                        .clickable { onViewAllJobsClick("Active") }
                        .padding(20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange, // Clipboard icon placeholder
                                contentDescription = "Active Jobs",
                                tint = YellowPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "$activeCount",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkColor
                            )
                            Text(
                                text = "Active Jobs",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = YellowText
                            )
                        }
                    }
                }

                // Right column stats
                val rightCardOffset by animateDpAsState(
                    targetValue = if (animateCards) 0.dp else 150.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "RightCardOffset"
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer(translationX = rightCardOffset.value),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Completed Card
                    StatsRowCard(
                        count = completedCount,
                        label = "COMPLETED",
                        iconBgColor = GreenLightBg,
                        iconColor = GreenCompleted,
                        icon = Icons.Default.Check,
                        onClick = { onViewAllJobsClick("Completed") }
                    )

                    // Pending Card
                    StatsRowCard(
                        count = pendingCount,
                        label = "PENDING",
                        iconBgColor = RedLightBg,
                        iconColor = RedPending,
                        icon = Icons.Default.Info, // Clock representation
                        onClick = { onViewAllJobsClick("Pending") }
                    )
                }
            }
        }

        // Up Next Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Up Next",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PurpleLightBg)
                        .clickable { onViewAllJobsClick("All") }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "View All >",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                }
            }
        }

        // Up Next Job Card
        item {
            if (upNextJob != null) {
                val jobCardScale by animateFloatAsState(
                    targetValue = if (animateCards) 1f else 0.8f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "JobCardScale"
                )
                
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val pressScale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "pressScale")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(scaleX = jobCardScale * pressScale, scaleY = jobCardScale * pressScale)
                        .clickable(interactionSource = interactionSource, indication = null) {
                            onJobClick(upNextJob.id)
                        },
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
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(YellowLightBg)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = upNextJob.id,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = YellowText
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Time",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = upNextJob.serviceTime,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                            }
                        }

                        Text(
                            text = upNextJob.serviceType,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Text(
                            text = upNextJob.location,
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No upcoming active jobs.",
                            color = TextMuted,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatsRowCard(
    count: Int,
    label: String,
    iconBgColor: Color,
    iconColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "StatsCardPress")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$count",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun HomeScreenShimmer(userName: String) {
    val brush = shimmerBrush()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hello, $userName",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Here is your daily overview",
                fontSize = 16.sp,
                color = TextMuted
            )
        }
        
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Active Card Shimmer
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(brush)
                )
                // Right Stats Cards Column Shimmer
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(20.dp)).background(brush))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(20.dp)).background(brush))
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Up Next",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        }
        
        item {
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
