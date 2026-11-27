package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.R

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold),
    Font(R.font.inter, FontWeight.ExtraBold),
)

@Immutable
data class SafePalette(
    val isDark: Boolean,
    val bgApp: Color,
    val bgSurface: Color,
    val bgSurfaceMuted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val borderSubtle: Color,
    val green500: Color,
    val green600: Color,
    val greenSoft: Color,
    val greenBorder: Color,
    val orange500: Color,
    val orange600: Color,
    val orangeSoft: Color,
    val orangeBorder: Color,
    val red500: Color,
    val red600: Color,
    val redSoft: Color,
    val redBorder: Color,
)

val LightSafePalette = SafePalette(
    isDark = false,
    bgApp = Color(0xFFF8FAFC),
    bgSurface = Color(0xFFFFFFFF),
    bgSurfaceMuted = Color(0xFFF1F5F9),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF64748B),
    borderSubtle = Color(0xFFE2E8F0),
    green500 = Color(0xFF10B981),
    green600 = Color(0xFF059669),
    greenSoft = Color(0xFFECFDF5),
    greenBorder = Color(0xFFA7F3D0),
    orange500 = Color(0xFFF59E0B),
    orange600 = Color(0xFFD97706),
    orangeSoft = Color(0xFFFFFBEB),
    orangeBorder = Color(0xFFFDE68A),
    red500 = Color(0xFFEF4444),
    red600 = Color(0xFFDC2626),
    redSoft = Color(0xFFFEF2F2),
    redBorder = Color(0xFFFECACA),
)

val DarkSafePalette = SafePalette(
    isDark = true,
    bgApp = Color(0xFF0B1120),
    bgSurface = Color(0xFF111827),
    bgSurfaceMuted = Color(0xFF1E293B),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF94A3B8),
    borderSubtle = Color(0xFF1E293B),
    green500 = Color(0xFF10B981),
    green600 = Color(0xFF34D399),
    greenSoft = Color(0x2410B981),
    greenBorder = Color(0x5910B981),
    orange500 = Color(0xFFF59E0B),
    orange600 = Color(0xFFFBBF24),
    orangeSoft = Color(0x24F59E0B),
    orangeBorder = Color(0x59F59E0B),
    red500 = Color(0xFFEF4444),
    red600 = Color(0xFFF87171),
    redSoft = Color(0x26EF4444),
    redBorder = Color(0x61EF4444),
)

val LocalSafePalette = staticCompositionLocalOf { LightSafePalette }

private val SafeTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 23.sp,
        lineHeight = 29.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

private val SafeShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun SafeTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkSafePalette else LightSafePalette
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF10B981),
            onPrimary = Color.White,
            secondary = Color(0xFF1E293B),
            onSecondary = Color(0xFFF8FAFC),
            background = palette.bgApp,
            onBackground = palette.textPrimary,
            surface = palette.bgSurface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.bgSurfaceMuted,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.borderSubtle,
            error = Color(0xFFEF4444),
            onError = Color.White,
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF059669),
            onPrimary = Color.White,
            secondary = Color(0xFFF1F5F9),
            onSecondary = Color(0xFF0F172A),
            background = palette.bgApp,
            onBackground = palette.textPrimary,
            surface = palette.bgSurface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.bgSurfaceMuted,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.borderSubtle,
            error = Color(0xFFDC2626),
            onError = Color.White,
        )
    }

    CompositionLocalProvider(LocalSafePalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SafeTypography,
            shapes = SafeShapes,
            content = content,
        )
    }
}
