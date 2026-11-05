package com.flagquiz.app.presentation.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AccuracyChart(
    accuracyPoints: List<Float>,
    modifier: Modifier = Modifier
) {
    if (accuracyPoints.isEmpty()) {
        Text(
            text = "Play at least one game to visualize your accuracy trend.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val width = size.width
            val height = size.height
            val spacing = if (accuracyPoints.size > 1) width / (accuracyPoints.size - 1) else width

            // 1. Draw horizontal percentage grid lines (0%, 50%, 100%)
            val gridLevels = listOf(0.0f, 0.5f, 1.0f)
            gridLevels.forEach { ratio ->
                val y = height - (height * ratio)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Build coordinates path
            val points = accuracyPoints.mapIndexed { index, value ->
                val normalizedValue = (value / 100f).coerceIn(0f, 1f)
                val x = if (accuracyPoints.size > 1) index * spacing else width / 2
                val y = height - (normalizedValue * height)
                Offset(x, y)
            }

            val strokePath = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
            }

            // 3. Draw gradient fill beneath the line
            if (points.size > 1) {
                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
            }

            // 4. Draw continuous stroke line
            drawPath(
                path = strokePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 5. Draw circular markers at each recorded data point
            points.forEach { point ->
                drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = point)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = point)
            }
        }

        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Oldest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Recent Games", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}