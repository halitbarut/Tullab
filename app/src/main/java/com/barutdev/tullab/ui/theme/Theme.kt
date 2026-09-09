package com.barutdev.tullab.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ProfessionalBlue,
    onPrimary = Color.White,
    primaryContainer = ProfessionalBlueLight,
    onPrimaryContainer = Color(0xFF062748),
    secondary = ProfessionalBlueDark,
    onSecondary = Color.White,
    secondaryContainer = ProfessionalBlue,
    onSecondaryContainer = Color.White,
    tertiary = ProfessionalBlueLight,
    onTertiary = Color(0xFF052145),
    background = TullabBackground,
    onBackground = TullabOnSurface,
    surface = TullabSurface,
    onSurface = TullabOnSurface,
    surfaceVariant = Color(0xFFE0E5ED),
    onSurfaceVariant = TullabOnSurfaceVariant,
    outline = Color(0xFFBEC6D4)
)

private val DarkColorScheme = darkColorScheme(
    primary = ProfessionalBlueLight,
    onPrimary = Color(0xFF01264A),
    primaryContainer = ProfessionalBlueDark,
    onPrimaryContainer = Color(0xFFE5F1FF),
    secondary = ProfessionalBlue,
    onSecondary = Color(0xFF031730),
    secondaryContainer = ProfessionalBlueDark,
    onSecondaryContainer = Color(0xFFD8E7FF),
    tertiary = ProfessionalBlue,
    onTertiary = Color(0xFF031730),
    background = Color(0xFF0D111C),
    onBackground = Color(0xFFE0E5ED),
    surface = Color(0xFF131826),
    onSurface = Color(0xFFE0E5ED),
    surfaceVariant = Color(0xFF2A3242),
    onSurfaceVariant = Color(0xFFC4CAD7),
    outline = Color(0xFF556070)
)

@Composable
fun TullabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
                !darkTheme
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars =
                !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
