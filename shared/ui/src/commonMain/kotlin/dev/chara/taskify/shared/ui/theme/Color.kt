package dev.chara.taskify.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.chara.taskify.shared.model.SeedColor
import dev.chara.taskify.shared.ui.theme.color.blend
import dev.chara.taskify.shared.ui.theme.color.harmonize

@Composable
fun SeedColor.toColor() = when (this) {
    SeedColor.Red -> Color(0xFFAC322C)
    SeedColor.Orange -> Color(0xFF974800)
    SeedColor.Yellow -> Color(0xFF7C5800)
    SeedColor.Green -> Color(0xFF356A22)
    SeedColor.Blue -> Color(0xFF00639D)
    SeedColor.Purple -> Color(0xFF6750A4)
    SeedColor.Pink -> Color(0xFF95416E)
}

@Composable
fun SeedColor.harmonizeWithParent() = toColor()
    .harmonize(LocalSeedColor.current)

@Composable
fun SeedColor.blendWithParent() = blendWith(LocalSeedColor.current)

@Composable
fun SeedColor.blendWith(parent: Color) = toColor()
    .blend(parent, 0.375)
    .harmonize(parent)

fun Color.harmonize(sourceColor: Color): Color {
    val blended = harmonize(toArgb(), sourceColor.toArgb())
    return Color(blended)
}

fun Color.blend(sourceColor: Color, amount: Double): Color {
    val blended = blend(toArgb(), sourceColor.toArgb(), amount)
    return Color(blended)
}