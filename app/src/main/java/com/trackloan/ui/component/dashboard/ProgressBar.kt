package com.trackloan.ui.component.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.trackloan.ui.theme.Green
import com.trackloan.ui.theme.Orange
import com.trackloan.ui.theme.Red

@Composable
fun ProgressBar(
    collected: Float,
    expected: Float,
    height: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val progress = if (expected > 0) collected / expected else 0f
    val animatedProgress = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, delayMillis = 400)
    )

    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val width = size.width
            val height = size.height

            // Background
            drawRoundRect(
                color = backgroundColor,
                topLeft = Offset(0f, 0f),
                size = Size(width, height),
                cornerRadius = CornerRadius(height / 2, height / 2)
            )

            // Progress
            val progressWidth = width * animatedProgress.value
            drawRoundRect(
                color = when {
                    animatedProgress.value >= 0.8f -> Green
                    animatedProgress.value >= 0.5f -> Orange
                    else -> Red
                },
                topLeft = Offset(0f, 0f),
                size = Size(progressWidth, height),
                cornerRadius = CornerRadius(height / 2, height / 2)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₹${collected.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = Green
            )
            Text(
                text = "₹${expected.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
