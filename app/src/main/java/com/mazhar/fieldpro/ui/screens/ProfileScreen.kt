package com.mazhar.fieldpro.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.fieldpro.data.FieldProRepository
import com.mazhar.fieldpro.data.User
import com.mazhar.fieldpro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    repository: FieldProRepository,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var isChangingPassword by remember { mutableStateOf(false) }

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
            text = "Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        // Profile Detail Banner
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
                // Avatar Circle
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
                        text = user.fullName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.role,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueText
                    )
                }

                // Email Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDarkColor
                    )
                }
            }
        }

        // Settings Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column {
                    SettingsRow(
                        title = "Account Preferences",
                        icon = Icons.Default.Settings,
                        onClick = {
                            Toast.makeText(context, "Preferences screen is coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    HorizontalDivider(color = CardBorder, thickness = 1.dp)
                    
                    SettingsRow(
                        title = "Privacy & Security (Change Password)",
                        icon = Icons.Default.Lock,
                        onClick = {
                            showChangePasswordDialog = true
                        }
                    )
                    
                    HorizontalDivider(color = CardBorder, thickness = 1.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 20.dp),
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

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isChangingPassword) {
                    showChangePasswordDialog = false
                    newPassword = ""
                    confirmNewPassword = ""
                }
            },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Enter a new password for your account.", color = TextMuted)
                    
                    Column {
                        Text(
                            text = "New Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            placeholder = { Text("••••••••") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isChangingPassword,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = CardBorder
                            )
                        )
                    }

                    Column {
                        Text(
                            text = "Confirm New Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = confirmNewPassword,
                            onValueChange = { confirmNewPassword = it },
                            placeholder = { Text("••••••••") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isChangingPassword,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = CardBorder
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword.length < 6) {
                            Toast.makeText(context, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                        } else if (newPassword != confirmNewPassword) {
                            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                        } else {
                            isChangingPassword = true
                            repository.updatePassword(
                                newPassword = newPassword,
                                onSuccess = {
                                    isChangingPassword = false
                                    showChangePasswordDialog = false
                                    newPassword = ""
                                    confirmNewPassword = ""
                                    Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_LONG).show()
                                },
                                onFailure = { err ->
                                    isChangingPassword = false
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    enabled = !isChangingPassword
                ) {
                    if (isChangingPassword) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Update")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showChangePasswordDialog = false 
                        newPassword = ""
                        confirmNewPassword = ""
                    },
                    enabled = !isChangingPassword
                ) {
                    Text("Cancel", color = TextDark)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SettingsRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 18.dp, horizontal = 20.dp),
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = TextMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}
