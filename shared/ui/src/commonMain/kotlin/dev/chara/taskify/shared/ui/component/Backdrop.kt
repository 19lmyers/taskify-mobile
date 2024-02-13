package dev.chara.taskify.shared.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BackdropScaffold(
    appBar: @Composable () -> Unit,
    appBarHeight: Dp,
    backLayerContent: @Composable () -> Unit,
    frontLayerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: BackdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
) {
    val backLayerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val frontLayerColor = MaterialTheme.colorScheme.surfaceContainerLow

    val insets = WindowInsets.systemBars.asPaddingValues()

    BackdropScaffold(
        modifier = modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)),
        appBar = {
            Surface(color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface) {
                appBar()
            }
        },
        backLayerContent = {
            Surface(color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface) {
                backLayerContent()
            }
        },
        frontLayerContent = {
            Surface(color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface) {
                frontLayerContent()
            }
        },
        scaffoldState = scaffoldState,
        gesturesEnabled = true,
        peekHeight = appBarHeight,
        headerHeight = 2 * appBarHeight + insets.calculateBottomPadding(),
        backLayerBackgroundColor = backLayerColor,
        frontLayerBackgroundColor = frontLayerColor,
        frontLayerElevation = 0.dp,
        frontLayerScrimColor = frontLayerColor.copy(alpha = 0.60f),
        frontLayerShape = MaterialTheme.shapes.extraLarge
    )
}