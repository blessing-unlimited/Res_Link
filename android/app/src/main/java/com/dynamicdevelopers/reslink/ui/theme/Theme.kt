package com.dynamicdevelopers.reslink.ui.theme

// Same palette as the web app: the logo's #04180f for brand surfaces, its greens for actions (Android Developers, 2025b).

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val BrandNight = Color(0xFF04180F)
val BrandNightRaised = Color(0xFF0D2E1F)
val Forest = Color(0xFF1F5A3E)
val ForestDark = Color(0xFF164430)
val Leaf = Color(0xFF6AA96E)
val LeafLight = Color(0xFF8FC98F)
val Mint = Color(0xFFE4F2E2)
val Page = Color(0xFFF5F6F2)
val CardWhite = Color(0xFFFFFFFF)
val Ink = Color(0xFF0E1A13)
val InkSoft = Color(0xFF334139)
val Muted = Color(0xFF69766E)
val Line = Color(0xFFE4E8E2)
val OnDark = Color(0xFFE8F1EA)
val OnDarkMuted = Color(0xB8E8F1EA)
val PointsGold = Color(0xFFF3C46B)
val Brass = Color(0xFFA66A0A)
val Danger = Color(0xFFB42318)

/** Foreground and background pair for one feature colour. */
data class Tint(val fg: Color, val bg: Color)

object Tints {
    val Leaf = Tint(Color(0xFF2C6B38), Color(0xFFE5F3E3))
    val Gold = Tint(Color(0xFFA66A0A), Color(0xFFFCF2DD))
    val Sky = Tint(Color(0xFF1D5F80), Color(0xFFE2EFF6))
    val Rose = Tint(Color(0xFFB42318), Color(0xFFFDECEB))
    val Plum = Tint(Color(0xFF5D3B82), Color(0xFFF1EAF8))
    val Slate = Tint(Color(0xFF44515A), Color(0xFFEDF0F1))
}

private val Scheme = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = ForestDark,
    secondary = Leaf,
    onSecondary = BrandNight,
    background = Page,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    surfaceVariant = Page,
    onSurfaceVariant = Muted,
    outline = Line,
    error = Danger,
    onError = Color.White
)

private val Type = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, letterSpacing = (-0.6).sp, color = Ink),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, letterSpacing = (-0.4).sp, color = Ink),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 19.sp, letterSpacing = (-0.2).sp, color = Ink),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp, color = Ink),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, color = Muted),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
)

@Composable
fun ResLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = Type, content = content)
}
