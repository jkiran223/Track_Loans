package com.trackloan.ui.component.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.trackloan.ui.theme.Green
import com.trackloan.ui.theme.Orange
import com.trackloan.ui.theme.Red

@Composable
fun DonutChart(
    data: List<Float>,
    colors: List<Color> = listOf(Green, Orange, Red),
    strokeWidth: Dp = 12.dp,
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val total = data.sum()
    val proportions = data.map { it / total }

    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProportions = proportions.map { proportion ->
        animateFloatAsState(
            targetValue = if (animationPlayed) proportion else 0f,
            animationSpec = tween(durationMillis = 1000, delayMillis = 0)
        )
    }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            var startAngle = -90f

            animatedProportions.forEachIndexed { index, animatedProportion ->
                val sweepAngle = animatedProportion.value * 360f
                val color = colors.getOrElse(index) { Color.Gray }

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )

                startAngle += sweepAngle
            }
        }
    }
}
