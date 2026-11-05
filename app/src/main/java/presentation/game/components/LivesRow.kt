package com.flagquiz.app.presentation.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.flagquiz.app.core.designsystem.theme.IncorrectRed

@Composable
fun LivesRow(
    totalLives: Int = 3,
    remainingLives: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalLives) {
            val isAlive = i <= remainingLives

            val scale by animateFloatAsState(
                targetValue = if (isAlive) 1.0f else 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "HeartScale_$i"
            )

            val tint by animateColorAsState(
                targetValue = if (isAlive) IncorrectRed else MaterialTheme.colorScheme.outlineVariant,
                label = "HeartColor_$i"
            )

            Icon(
                imageVector = if (isAlive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isAlive) "Active Life" else "Lost Life",
                tint = tint,
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
            )
        }
    }
}