package com.expensetracker.app.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand seed colors
val Teal600 = Color(0xFF00897B)
val Orange400 = Color(0xFFFFA726)
val DeepPurple400 = Color(0xFF7E57C2)

// Semantic colors
val IncomeGreen = Color(0xFF43A047)
val ExpenseRed = Color(0xFFE53935)
val WarningOrange = Color(0xFFFF9800)

// Light scheme tonal colors
private val TealContainerLight = Color(0xFFB2DFDB)
private val OnTealContainerLight = Color(0xFF002016)
private val OrangeContainerLight = Color(0xFFFFE0B2)
private val OnOrangeContainerLight = Color(0xFF4A1E00)
private val PurpleContainerLight = Color(0xFFEDE7F6)
private val OnPurpleContainerLight = Color(0xFF21005D)

// Light scheme surface colors
private val BackgroundLight = Color(0xFFFBFDF9)
private val SurfaceLight = Color(0xFFFBFDF9)
private val SurfaceVariantLight = Color(0xFFDAE5E1)
private val OnSurfaceVariantLight = Color(0xFF3F4946)
private val OutlineLight = Color(0xFF6F7975)

// Dark scheme surface colors
private val BackgroundDark = Color(0xFF191C1B)
private val SurfaceDark = Color(0xFF191C1B)
private val SurfaceVariantDark = Color(0xFF3F4946)
private val OnSurfaceVariantDark = Color(0xFFBEC9C4)
private val OutlineDark = Color(0xFF899390)

val LightColorScheme = lightColorScheme(
    primary = Teal600,
    onPrimary = Color.White,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = OnTealContainerLight,
    secondary = Orange400,
    onSecondary = Color.White,
    secondaryContainer = OrangeContainerLight,
    onSecondaryContainer = OnOrangeContainerLight,
    tertiary = DeepPurple400,
    onTertiary = Color.White,
    tertiaryContainer = PurpleContainerLight,
    onTertiaryContainer = OnPurpleContainerLight,
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = BackgroundLight,
    onBackground = Color(0xFF191C1B),
    surface = SurfaceLight,
    onSurface = Color(0xFF191C1B),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF00504A),
    onPrimaryContainer = TealContainerLight,
    secondary = Color(0xFFFFCC80),
    onSecondary = Color(0xFF4A1E00),
    secondaryContainer = Color(0xFF6B2D00),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFFCE93D8),
    onTertiary = Color(0xFF4A148C),
    tertiaryContainer = Color(0xFF6A1B9A),
    onTertiaryContainer = Color(0xFFF3E5F5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = BackgroundDark,
    onBackground = Color(0xFFE1E3E1),
    surface = SurfaceDark,
    onSurface = Color(0xFFE1E3E1),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
)

val CategoryPalette: List<Color> = listOf(
    Color(0xFFEF5350),
    Color(0xFFEC407A),
    Color(0xFFAB47BC),
    Color(0xFF7E57C2),
    Color(0xFF5C6BC0),
    Color(0xFF42A5F5),
    Color(0xFF29B6F6),
    Color(0xFF26C6DA),
    Color(0xFF26A69A),
    Color(0xFF66BB6A),
    Color(0xFF9CCC65),
    Color(0xFFD4E157),
    Color(0xFFFFEE58),
    Color(0xFFFFCA28),
    Color(0xFFFFA726),
    Color(0xFFFF7043),
)
