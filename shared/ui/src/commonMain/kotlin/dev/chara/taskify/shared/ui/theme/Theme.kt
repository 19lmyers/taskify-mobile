package dev.chara.taskify.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.MaterialDynamicColors
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.SchemeContent
import com.materialkolor.scheme.SchemeExpressive
import com.materialkolor.scheme.SchemeFidelity
import com.materialkolor.scheme.SchemeFruitSalad
import com.materialkolor.scheme.SchemeMonochrome
import com.materialkolor.scheme.SchemeNeutral
import com.materialkolor.scheme.SchemeRainbow
import com.materialkolor.scheme.SchemeTonalSpot
import com.materialkolor.scheme.SchemeVibrant

val LocalDarkTheme = staticCompositionLocalOf { false }

val LocalBaseColor = staticCompositionLocalOf { Color(0xFF6750A4) }

val LocalSeedColor = compositionLocalOf { Color(0xFF6750A4) }

val LocalPaletteStyle = staticCompositionLocalOf { PaletteStyle.Vibrant }


@Composable
expect fun getPlatformColor(fallback: Color): Color

@Composable
fun BaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    style: PaletteStyle = PaletteStyle.Vibrant,
    content: @Composable () -> Unit
) {
    val default = Color(0xFF6750A4)

    val baseColor =
        if (dynamicColor) {
            getPlatformColor(default)
        } else {
            default
        }

    val colorScheme: ColorScheme by remember(baseColor, darkTheme, style) {
        derivedStateOf {
            dynamicColorScheme(
                seedColor = baseColor,
                isDark = darkTheme,
                style = style
            )
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalBaseColor provides baseColor,
        LocalSeedColor provides baseColor,
        LocalPaletteStyle provides style
    ) {
        MaterialTheme(colorScheme = colorScheme) {
            content()
        }
    }
}

@Composable
fun ColorTheme(
    seed: Color,
    darkTheme: Boolean = LocalDarkTheme.current,
    style: PaletteStyle = PaletteStyle.Vibrant,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme by remember(seed, darkTheme, style) {
        derivedStateOf {
            dynamicColorScheme(
                seedColor = seed,
                isDark = darkTheme,
                style = style
            )
        }
    }
    CompositionLocalProvider(
        LocalSeedColor provides seed,
        LocalPaletteStyle provides style
    ) {
        MaterialTheme(
            colorScheme = colorScheme
        ) {
            content()
        }
    }
}

fun dynamicColorScheme(
    seedColor: Color,
    isDark: Boolean,
    style: PaletteStyle = PaletteStyle.TonalSpot,
    contrastLevel: Double = 0.0,
): ColorScheme {
    val hct = Hct.fromInt(seedColor.toArgb())
    val colors = MaterialDynamicColors()
    val scheme = when (style) {
        PaletteStyle.TonalSpot -> SchemeTonalSpot(hct, isDark, contrastLevel)
        PaletteStyle.Neutral -> SchemeNeutral(hct, isDark, contrastLevel)
        PaletteStyle.Vibrant -> SchemeVibrant(hct, isDark, contrastLevel)
        PaletteStyle.Expressive -> SchemeExpressive(hct, isDark, contrastLevel)
        PaletteStyle.Rainbow -> SchemeRainbow(hct, isDark, contrastLevel)
        PaletteStyle.FruitSalad -> SchemeFruitSalad(hct, isDark, contrastLevel)
        PaletteStyle.Monochrome -> SchemeMonochrome(hct, isDark, contrastLevel)
        PaletteStyle.Fidelity -> SchemeFidelity(hct, isDark, contrastLevel)
        PaletteStyle.Content -> SchemeContent(hct, isDark, contrastLevel)
    }

    return ColorScheme(
        background = Color(colors.background().getArgb(scheme)),
        error = Color(colors.error().getArgb(scheme)),
        errorContainer = Color(colors.errorContainer().getArgb(scheme)),
        inverseOnSurface = Color(colors.inverseOnSurface().getArgb(scheme)),
        inversePrimary = Color(colors.inversePrimary().getArgb(scheme)),
        inverseSurface = Color(colors.inverseSurface().getArgb(scheme)),
        onBackground = Color(colors.onBackground().getArgb(scheme)),
        onError = Color(colors.onError().getArgb(scheme)),
        onErrorContainer = Color(colors.onErrorContainer().getArgb(scheme)),
        onPrimary = Color(colors.onPrimary().getArgb(scheme)),
        onPrimaryContainer = Color(colors.onPrimaryContainer().getArgb(scheme)),
        onSecondary = Color(colors.onSecondary().getArgb(scheme)),
        onSecondaryContainer = Color(colors.onSecondaryContainer().getArgb(scheme)),
        onSurface = Color(colors.onSurface().getArgb(scheme)),
        onSurfaceVariant = Color(colors.onSurfaceVariant().getArgb(scheme)),
        onTertiary = Color(colors.onTertiary().getArgb(scheme)),
        onTertiaryContainer = Color(colors.onTertiaryContainer().getArgb(scheme)),
        outline = Color(colors.outline().getArgb(scheme)),
        outlineVariant = Color(colors.outlineVariant().getArgb(scheme)),
        primary = Color(colors.primary().getArgb(scheme)),
        primaryContainer = Color(colors.primaryContainer().getArgb(scheme)),
        scrim = Color(colors.scrim().getArgb(scheme)),
        secondary = Color(colors.secondary().getArgb(scheme)),
        secondaryContainer = Color(colors.secondaryContainer().getArgb(scheme)),
        surface = Color(colors.surface().getArgb(scheme)),
        surfaceTint = Color(colors.surfaceTint().getArgb(scheme)),
        surfaceVariant = Color(colors.surfaceVariant().getArgb(scheme)),
        tertiary = Color(colors.tertiary().getArgb(scheme)),
        tertiaryContainer = Color(colors.tertiaryContainer().getArgb(scheme)),
        surfaceBright = Color(colors.surfaceBright().getArgb(scheme)),
        surfaceDim = Color(colors.surfaceDim().getArgb(scheme)),
        surfaceContainer = Color(colors.surfaceContainer().getArgb(scheme)),
        surfaceContainerHigh = Color(colors.surfaceContainerHigh().getArgb(scheme)),
        surfaceContainerHighest = Color(colors.surfaceContainerHighest().getArgb(scheme)),
        surfaceContainerLow = Color(colors.surfaceContainerLow().getArgb(scheme)),
        surfaceContainerLowest = Color(colors.surfaceContainerLowest().getArgb(scheme))
    )
}