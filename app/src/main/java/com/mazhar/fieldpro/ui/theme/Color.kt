package com.mazhar.fieldpro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// FieldPro Specific Colors
val BluePrimary = Color(0xFF0D59D6)
val BlueLightBg = Color(0xFFD2E3FC)
val BlueText = Color(0xFF1967D2)

val PurplePrimary = Color(0xFF5A4EA3)
val PurpleLightBg = Color(0xFFE8E5F3)

val GreenCompleted = Color(0xFF127A3A)
val GreenLightBg = Color(0xFFE6F4EA)

val RedPending = Color(0xFFD93025)
val RedLightBg = Color(0xFFFCE8E6)

val BackgroundLightColor = Color(0xFFF8FAFC)
val CardBorderColor = Color(0xFFE2E8F0)
val TextDarkColor = Color(0xFF1E293B)
val TextMutedColor = Color(0xFF64748B)

val BackgroundLight: Color @Composable get() = MaterialTheme.colorScheme.background
val CardBorder: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
val TextDark: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val TextMuted: Color @Composable get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF94A3B8) else TextMutedColor
val CardBg: Color @Composable get() = MaterialTheme.colorScheme.surface