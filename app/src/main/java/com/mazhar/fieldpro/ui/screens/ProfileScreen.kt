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
import com.mazhar.fieldpro.CustomToastManager
import com.mazhar.fieldpro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    repository: FieldProRepository,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onProfileUpdated: (User) -> Unit
) {
    val context = LocalContext.current
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var isChangingPassword by remember { mutableStateOf(false) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editFullName by remember { mutableStateOf(user.fullName) }
    var editContactNumber by remember { mutableStateOf(user.contactNumber) }
    var editExpertise by remember { mutableStateOf(user.expertise) }
    var isUpdatingProfile by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        editFullName = user.fullName
        editContactNumber = user.contactNumber
        editExpertise = user.expertise
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        // Profile Detail Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEditProfileDialog = true },
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
                        color = YellowText
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

                if (user.contactNumber.isNotEmpty() || user.expertise.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (user.contactNumber.isNotEmpty()) {
                            Text(
                                text = "📞 ${user.contactNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkColor
                            )
                        }
                        if (user.expertise.isNotEmpty()) {
                            Text(
                                text = "🛠️ ${user.expertise}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = YellowText
                            )
                        }
                    }
                }
                
                Text(
                    text = "Tap card to edit your details",
                    fontSize = 11.sp,
                    color = YellowText.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
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
                            CustomToastManager.showToast("Preferences screen is coming soon!")
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
                            colors = SwitchDefaults.colors(checkedThumbColor = YellowPrimary)
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
                                focusedBorderColor = YellowPrimary,
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
                                focusedBorderColor = YellowPrimary,
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
                            CustomToastManager.showToast("Password must be at least 6 characters.", isErrorToast = true)
                        } else if (newPassword != confirmNewPassword) {
                            CustomToastManager.showToast("Passwords do not match.", isErrorToast = true)
                        } else {
                            isChangingPassword = true
                            repository.updatePassword(
                                newPassword = newPassword,
                                onSuccess = {
                                    isChangingPassword = false
                                    showChangePasswordDialog = false
                                    newPassword = ""
                                    confirmNewPassword = ""
                                    CustomToastManager.showToast("Password updated successfully!")
                                },
                                onFailure = { err ->
                                    isChangingPassword = false
                                    CustomToastManager.showToast(err, isErrorToast = true)
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
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

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isUpdatingProfile) {
                    showEditProfileDialog = false
                }
            },
            title = { Text("Edit Profile Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Modify your profile details below.", color = TextMuted)

                    Column {
                        Text(
                            text = "Full Name",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = editFullName,
                            onValueChange = { editFullName = it },
                            placeholder = { Text("e.g. Alex Johnson") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isUpdatingProfile,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YellowPrimary,
                                unfocusedBorderColor = CardBorder
                            )
                        )
                    }

                    Column {
                        Text(
                            text = "Contact Number",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = editContactNumber,
                            onValueChange = { editContactNumber = it },
                            placeholder = { Text("e.g. +1 555-0199") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isUpdatingProfile,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YellowPrimary,
                                unfocusedBorderColor = CardBorder
                            )
                        )
                    }

                    Column {
                        Text(
                            text = "Expertise / Skills",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = editExpertise,
                            onValueChange = { editExpertise = it },
                            placeholder = { Text("e.g. HVAC / Electrical Repair") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isUpdatingProfile,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YellowPrimary,
                                unfocusedBorderColor = CardBorder
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editFullName.isBlank()) {
                            CustomToastManager.showToast("Full Name cannot be empty.", isErrorToast = true)
                        } else {
                            isUpdatingProfile = true
                            repository.updateUserProfile(
                                fullName = editFullName.trim(),
                                contactNumber = editContactNumber.trim(),
                                expertise = editExpertise.trim(),
                                onSuccess = { updatedUser ->
                                    isUpdatingProfile = false
                                    showEditProfileDialog = false
                                    onProfileUpdated(updatedUser)
                                    CustomToastManager.showToast("Profile updated successfully!")
                                },
                                onFailure = { err ->
                                    isUpdatingProfile = false
                                    CustomToastManager.showToast(err, isErrorToast = true)
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
                    enabled = !isUpdatingProfile
                ) {
                    if (isUpdatingProfile) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditProfileDialog = false
                        editFullName = user.fullName
                        editContactNumber = user.contactNumber
                        editExpertise = user.expertise
                    },
                    enabled = !isUpdatingProfile
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
