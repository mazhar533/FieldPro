package com.mazhar.fieldpro.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.fieldpro.data.FieldProRepository
import com.mazhar.fieldpro.data.User
import com.mazhar.fieldpro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    repository: FieldProRepository,
    onLoginSuccess: (User) -> Unit
) {
    val context = LocalContext.current

    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Register specific states
    var fullName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("TECHNICIAN") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Forgot password state
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    // Animations for entrance
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(isRegisterMode) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
        )
    }

    val translateY = (1f - animProgress.value) * 30f
    val opacity = animProgress.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .graphicsLayer(
                    translationY = translateY,
                    alpha = opacity
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Icon Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(BlueLightBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Logo",
                    tint = BluePrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isRegisterMode) "Create Account" else "FieldPro",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isRegisterMode) "Register as a service technician" else "Manage your jobs anywhere.",
                fontSize = 16.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Registration Fields
            AnimatedVisibility(visible = isRegisterMode) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Full Name Field
                    Text(
                        text = "Full Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { 
                            fullName = it
                            errorMessage = ""
                        },
                        placeholder = { Text("John Doe", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact Number
                    Text(
                        text = "Contact Number",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { 
                            contactNumber = it
                            errorMessage = ""
                        },
                        placeholder = { Text("+1 555-0199", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role Chip Selection
                    Text(
                        text = "Role",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("TECHNICIAN", "ADMIN").forEach { r ->
                            val selected = role == r
                            FilterChip(
                                selected = selected,
                                onClick = { role = r },
                                label = { Text(r) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BluePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = CardBg,
                                    labelColor = TextDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Email Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Email Address",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        errorMessage = ""
                    },
                    placeholder = { Text("tech@example.com", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = ""
                    },
                    placeholder = { Text("••••••••", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    )
                )
            }

            // Confirm Password Field (Register Mode)
            AnimatedVisibility(visible = isRegisterMode) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Text(
                        text = "Confirm Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            errorMessage = ""
                        },
                        placeholder = { Text("••••••••", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        )
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sign In / Sign Up Button
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "ButtonScale"
            )

            Button(
                onClick = {
                    if (isRegisterMode) {
                        if (fullName.isBlank()) {
                            errorMessage = "Please enter your full name."
                        } else if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            errorMessage = "Please enter a valid email address."
                        } else if (contactNumber.isBlank()) {
                            errorMessage = "Please enter your contact number."
                        } else if (password.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                        } else if (password != confirmPassword) {
                            errorMessage = "Passwords do not match."
                        } else {
                            isLoading = true
                            errorMessage = ""
                            val newUser = User(fullName, email, contactNumber, role)
                            repository.registerUser(
                                user = newUser,
                                password = password,
                                onSuccess = {
                                    isLoading = false
                                    isRegisterMode = false
                                    password = ""
                                    confirmPassword = ""
                                    Toast.makeText(context, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                                },
                                onFailure = { err ->
                                    isLoading = false
                                    errorMessage = err
                                }
                            )
                        }
                    } else {
                        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            errorMessage = "Please enter a valid email address."
                        } else if (password.isEmpty()) {
                            errorMessage = "Please enter your password."
                        } else {
                            isLoading = true
                            errorMessage = ""
                            repository.authenticateUser(
                                email = email,
                                password = password,
                                onSuccess = { user ->
                                    isLoading = false
                                    onLoginSuccess(user)
                                },
                                onFailure = { err ->
                                    isLoading = false
                                    errorMessage = err
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = Color.White
                ),
                enabled = !isLoading,
                interactionSource = interactionSource
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = if (isRegisterMode) "Sign Up" else "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Toggle Mode Link
            Text(
                text = if (isRegisterMode) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = BlueText,
                modifier = Modifier
                    .clickable { 
                        if (!isLoading) {
                            isRegisterMode = !isRegisterMode
                            errorMessage = ""
                        }
                    }
                    .padding(8.dp)
            )

            if (!isRegisterMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Forgot password?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    modifier = Modifier
                        .clickable { 
                            if (!isLoading) {
                                showForgotPasswordDialog = true
                            }
                        }
                        .padding(8.dp)
                )
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your email address to receive a password reset link.", color = TextMuted)
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        placeholder = { Text("tech@example.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                            repository.resetPassword(
                                email = resetEmail,
                                onSuccess = {
                                    showForgotPasswordDialog = false
                                    resetEmail = ""
                                    Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_LONG).show()
                                },
                                onFailure = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = TextDark)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
