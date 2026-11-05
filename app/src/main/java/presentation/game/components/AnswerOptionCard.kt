package com.flagquiz.app.presentation.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flagquiz.app.core.designsystem.theme.CorrectGreen
import com.flagquiz.app.core.designsystem.theme.CorrectGreenContainer
import com.flagquiz.app.core.designsystem.theme.IncorrectRed
import com.flagquiz.app.core.designsystem.theme.IncorrectRedContainer
import com.flagquiz.app.domain.model.AnswerOption
import com.flagquiz.app.domain.model.GameMode

@Composable
fun AnswerOptionCard(
    option: AnswerOption,
    gameMode: GameMode,
    isAnswerRevealed: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (option.isHiddenByHint) {
        Spacer(modifier = modifier.height(56.dp))
        return
    }

    val isCorrect = option.isCorrect

    val targetContainerColor = when {
        !isAnswerRevealed -> MaterialTheme.colorScheme.surface
        isCorrect -> CorrectGreenContainer
        isSelected && !isCorrect -> IncorrectRedContainer
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }

    val targetContentColor = when {
        !isAnswerRevealed -> MaterialTheme.colorScheme.onSurface
        isCorrect -> CorrectGreen
        isSelected && !isCorrect -> IncorrectRed
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }

    val animatedColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "CardColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isAnswerRevealed && (isCorrect || isSelected)) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "CardScale"
    )

    val borderStroke = when {
        isAnswerRevealed && isCorrect -> BorderStroke(2.dp, CorrectGreen)
        isAnswerRevealed && isSelected && !isCorrect -> BorderStroke(2.dp, IncorrectRed)
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Card(
        onClick = onClick,
        enabled = !isAnswerRevealed,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = animatedColor,
            contentColor = targetContentColor
        ),
        border = borderStroke,
        modifier = modifier
            .fillMaxWidth()
            .height(if (gameMode == GameMode.NAME_TO_FLAG) 110.dp else 56.dp)
            .scale(scale)
            .semantics {
                role = Role.Button
                contentDescription = "${option.text}. ${if (isAnswerRevealed && isCorrect) "Correct" else ""}"
            }
    ) {
        if (gameMode == GameMode.NAME_TO_FLAG) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(option.flagUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = option.text,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                if (isAnswerRevealed) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        if (isCorrect) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Correct",
                                tint = CorrectGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Incorrect",
                                tint = IncorrectRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option.text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isAnswerRevealed && (isCorrect || isSelected)) FontWeight.Bold else FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (isAnswerRevealed) {
                    if (isCorrect) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Correct",
                            tint = CorrectGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Incorrect",
                            tint = IncorrectRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}