package dev.chara.taskify.shared.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chara.taskify.shared.model.SeedColor
import dev.chara.taskify.shared.ui.theme.LocalDarkTheme
import dev.chara.taskify.shared.ui.theme.LocalPaletteStyle
import dev.chara.taskify.shared.ui.theme.blendWithParent
import dev.chara.taskify.shared.ui.theme.dynamicColorScheme

@Composable
fun ColorSwatch(
    seed: SeedColor?,
    outline: Color,
    selection: Color,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    val darkTheme = LocalDarkTheme.current
    val style = LocalPaletteStyle.current

    val modifier =
        if (seed != null) {
            val colorScheme = dynamicColorScheme(seed.blendWithParent(), darkTheme, style)
            Modifier.background(colorScheme.surfaceContainerHigh, MaterialTheme.shapes.medium)
        } else {
            Modifier.background(
                Brush.sweepGradient(
                    SeedColor.entries.map { listColor ->
                        val colorScheme =
                            dynamicColorScheme(listColor.blendWithParent(), darkTheme, style)
                        colorScheme.surfaceContainerHigh
                    }
                ),
                MaterialTheme.shapes.medium
            )
        }

    Surface(
        modifier = Modifier.requiredSize(72.dp).padding(8.dp).then(modifier),
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        border =
        BorderStroke(
            2.dp,
            if (selected) {
                selection
            } else {
                outline
            }
        )
    ) {
        Box(modifier = Modifier.clickable { onSelected() }) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.align(Alignment.Center),
                    tint = selection
                )
            }
        }
    }
}