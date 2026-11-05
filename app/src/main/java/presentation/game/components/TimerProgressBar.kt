package com.flagquiz.app.presentation.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.flagquiz.app.core.designsystem.theme.CorrectGreen
import com.flagquiz.app.core.designsystem.theme.IncorrectRed
import com.flagquiz.app.core.designsystem.theme.TimerProgressGold

@Composable
fun TimerProgressBar(
    progress: Float,
    isTimeWarning: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "TimerProgress"
    )

    val targetColor = when {
        isTimeWarning -> IncorrectRed
        progress < 0.5f -> TimerProgressGold
        else -> CorrectGreen
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "TimerColor"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = animatedColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}