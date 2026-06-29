package com.mazhar.fieldpro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//val Purple80 = Color(0xFFD0BCFF)
//val PurpleGrey80 = Color(0xFCC2DC)
//val Pink80 = Color(0xEFFETE8C8)

//val Purple40 = Color(0xFF6650a4)
//val PurpleGrey40 = Color(0xFF625b71)
//val Pink40 = Color(0xFF7D5260)

// FieldPro Specific Colors (Premium Yellow & White Theme)
val YellowPrimary = Color(0xFFFAC630) // Brighter Vibrant Yellow capsule
val YellowLightBg = Color(0xFFFEF9C3) // Soft light yellow tint
val YellowText = Color(0xFFEAB308) // Brighter Gold/Yellow text

val PurplePrimary = Color(0xFFD97706) // Amber Accent
val PurpleLightBg = Color(0xFFFEF3C7) // Amber Accent Light

val GreenCompleted = Color(0xFF16A34A) // Green for Completed status
val GreenLightBg = Color(0xFFDCFCE7) // Soft Green Light background

val RedPending = Color(0xFFDC2626) // Red for Pending status
val RedLightBg = Color(0xFFFEE2E2) // Soft Red Light background

val BackgroundLightColor = Color(0xFFFFFDF5) // Ivory / soft cream white background
val CardBorderColor = Color(0xFFFEF08A) // Soft yellow card borders
val TextDarkColor = Color(0xFF1E293B) // Dark charcoal for primary texts
val TextMutedColor = Color(0xFF64748B) // Slate gray for secondary texts

val BackgroundLight: Color @Composable get() = MaterialTheme.colorScheme.background
val CardBorder: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
val TextDark: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val TextMuted: Color @Composable get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A) || MaterialTheme.colorScheme.background == Color(0xFF121200)) Color(0xFF94A3B8) else TextMutedColor
val CardBg: Color @Composable get() = MaterialTheme.colorScheme.surface