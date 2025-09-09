package com.trackloan.ui.component.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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

@Composable
fun BarChart(
    data: List<Float>,
    labels: List<String> = listOf("Disbursed", "Closing"),
    colors: List<Color> = listOf(Green, Orange),
    height: Dp = 60.dp,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOrNull() ?: 1f
    val proportions = data.map { it / maxValue }

    val animatedProportions = proportions.map { proportion ->
        animateFloatAsState(
            targetValue = proportion,
            animationSpec = tween(durationMillis = 1000, delayMillis = 200)
        )
    }

    Row(
        modifier = modifier.height(height),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        animatedProportions.forEachIndexed { index, animatedProportion ->
            val color = colors.getOrElse(index) { MaterialTheme.colorScheme.primary }
            val label = labels.getOrElse(index) { "" }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val barWidth = size.width
                    val barHeight = size.height * animatedProportion.value

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(0f, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                androidx.compose.material3.Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
