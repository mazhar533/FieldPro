package com.mazhar.fieldpro.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import com.mazhar.fieldpro.ui.theme.*
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.window.Dialog
import com.mazhar.fieldpro.CustomToastManager

fun uriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        
        val maxDimension = 640
        val width = originalBitmap.width
        val height = originalBitmap.height
        val (newWidth, newHeight) = if (width > height) {
            val ratio = width.toFloat() / height.toFloat()
            if (width > maxDimension) {
                Pair(maxDimension, (maxDimension / ratio).toInt())
            } else {
                Pair(width, height)
            }
        } else {
            val ratio = height.toFloat() / width.toFloat()
            if (height > maxDimension) {
                Pair((maxDimension / ratio).toInt(), maxDimension)
            } else {
                Pair(width, height)
            }
        }
        
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val bytes = outputStream.toByteArray()
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceReportFormScreen(
    jobId: String,
    onBackClick: () -> Unit,
    onSubmitSuccess: (findings: String, actionsTaken: String, remarks: String, evidenceImageBase64: String?) -> Unit
) {
    var findings by remember { mutableStateOf("") }
    var actionsTaken by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var evidenceBase64 by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var isQrVerified by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val base64 = uriToBase64(context, it)
            if (base64 != null) {
                evidenceBase64 = base64
            }
        }
    }

    // Bounce entrance animation for fields
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
        )
    }

    val translateY = (1f - animationProgress.value) * 60f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Service Report",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
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
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .graphicsLayer(translationY = translateY),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Job ID: $jobId",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = YellowText,
                modifier = Modifier
                    .background(YellowLightBg, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            // Findings Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Findings",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = findings,
                    onValueChange = { 
                        findings = it
                        showError = false
                    },
                    placeholder = { Text("What issues did you discover?", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    )
                )
            }

            // Actions Taken Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Actions Taken",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = actionsTaken,
                    onValueChange = { 
                        actionsTaken = it
                        showError = false
                    },
                    placeholder = { Text("What repairs/actions did you perform?", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    )
                )
            }

            // Remarks Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Completion Remarks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    placeholder = { Text("Any additional notes or comments...", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    )
                )
            }
            // Photo Attachment Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Attach Evidence Photo (Optional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (evidenceBase64.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.White, shape = RoundedCornerShape(16.dp))
                            .clickable {
                                galleryLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Add Photo",
                            color = YellowPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    val bitmap = remember(evidenceBase64) {
                        try {
                            val decodedString = Base64.decode(evidenceBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.White, shape = RoundedCornerShape(16.dp))
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Evidence Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        
                        // Delete Button Overlay
                        IconButton(
                            onClick = { evidenceBase64 = "" },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Photo",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // QR Code-Based Job Verification Card
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
                        text = "Onsite Job Verification (Required)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isQrVerified) GreenCompleted else RedPending)
                            )
                            Text(
                                text = if (isQrVerified) "Onsite QR Verified" else "Pending QR Verification",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isQrVerified) GreenCompleted else RedPending
                            )
                        }
                        
                        if (!isQrVerified) {
                            Button(
                                onClick = { showQrScanner = true },
                                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Scan QR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (showError) {
                Text(
                    text = "Findings and Actions Taken are required fields.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    if (findings.isBlank() || actionsTaken.isBlank()) {
                        showError = true
                    } else if (!isQrVerified) {
                        CustomToastManager.showToast("Please verify the job via QR Code first.", isErrorToast = true)
                    } else {
                        onSubmitSuccess(findings, actionsTaken, remarks, evidenceBase64.ifEmpty { null })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isQrVerified) GreenCompleted else Color.Gray
                )
            ) {
                Text(
                    text = if (isQrVerified) "Submit Report" else "Verification Required",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    if (showQrScanner) {
        Dialog(onDismissRequest = { showQrScanner = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = CardBg
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Scan Onsite QR Code",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        text = "Align the customer's equipment verification tag QR code within the frame to verify location.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // simulated viewfinder frame
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.Black, shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // simulating camera scan lines and dots
                        val infiniteTransition = rememberInfiniteTransition(label = "scanner")
                        val scannerY by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 200f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = androidx.compose.animation.core.LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scanline"
                        )

                        // Camera Viewfinder Background Sim
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .background(Color(0xFF2C3E50), shape = RoundedCornerShape(12.dp))
                        ) {
                            // Target brackets
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Draw mock QR code inside
                                Text(
                                    text = "QR: $jobId",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                            
                            // Laser horizontal scanline
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .graphicsLayer(translationY = scannerY)
                                    .background(YellowPrimary)
                            )
                        }
                    }

                    // Scan status logic
                    var scanComplete by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        scanComplete = true
                    }

                    if (scanComplete) {
                        Text(
                            text = "✅ Verification Tag Matched!",
                            color = GreenCompleted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Button(
                            onClick = {
                                isQrVerified = true
                                showQrScanner = false
                                CustomToastManager.showToast("Job location verified onsite!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Confirm & Continue", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = YellowPrimary, strokeWidth = 2.dp)
                            Text(
                                text = "Scanning code...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                        
                        TextButton(onClick = { showQrScanner = false }) {
                            Text("Cancel", color = TextDark)
                        }
                    }
                }
            }
        }
    }
}
