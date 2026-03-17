package com.example.receiptscanner.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.receiptscanner.ui.theme.GlassBackgroundDark
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.receiptscanner.ui.theme.GlassHighlightDark
import com.example.receiptscanner.ui.theme.GlassHighlightLight
import com.example.receiptscanner.ui.theme.GlassStrokeDark
import com.example.receiptscanner.ui.theme.GlassStrokeLight
import com.example.receiptscanner.ui.theme.GlassSurfaceDark
import com.example.receiptscanner.ui.theme.GlassSurfaceLight

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    glassColor: Color = glassSurfaceColor(),
    borderColor: Color = glassStrokeColor(),
    shadowElevation: Dp = 10.dp,
    blurRadius: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(blurRadius)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        shadowElevation = shadowElevation,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.clip(shape)) {
            // Blur only the glass background so content stays sharp.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(blurModifier)
                    .background(glassColor)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, borderColor, shape)
                    .background(glassHighlightColor(), shape)
            )
            Box {
                content()
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    content: @Composable BoxScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier,
        shape = shape,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = glassSurfaceColor(),
            scrolledContainerColor = glassSurfaceColor(),
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

@Composable
private fun isLocalDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.background == GlassBackgroundDark
}

@Composable
private fun glassSurfaceColor(): Color {
    return if (isLocalDarkTheme()) GlassSurfaceDark else GlassSurfaceLight
}

@Composable
private fun glassStrokeColor(): Color {
    return if (isLocalDarkTheme()) GlassStrokeDark else GlassStrokeLight
}

@Composable
private fun glassHighlightColor(): Color {
    return if (isLocalDarkTheme()) GlassHighlightDark else GlassHighlightLight
}
