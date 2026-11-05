package com.flagquiz.app.presentation.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flagquiz.app.core.designsystem.theme.CorrectGreen
import com.flagquiz.app.core.designsystem.theme.TimerProgressGold
import com.flagquiz.app.presentation.game.components.AnswerOptionCard
import com.flagquiz.app.presentation.game.components.TimerProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Challenge") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            when (val state = uiState) {
                is DailyChallengeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is DailyChallengeUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.checkDailyStatusAndInitialize() }) {
                            Text("Retry")
                        }
                    }
                }

                is DailyChallengeUiState.AlreadyCompleted -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Challenge Completed!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "You have already completed today's challenge.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth(0.85f)) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = TimerProgressGold, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "${state.streak}-Day Streak!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Next challenge unlocks in ~${state.hoursUntilNextChallenge} hours.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth(0.85f)) {
                            Text("Back to Dashboard")
                        }
                    }
                }

                is DailyChallengeUiState.CompletedToday -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Today's Challenge Finished!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Score: ${state.finalScore} points (${state.totalQuestions} questions)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "🔥 Streak increased to ${state.newStreak} days!", style = MaterialTheme.typography.titleLarge, color = TimerProgressGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth(0.85f)) {
                            Text("Done")
                        }
                    }
                }

                is DailyChallengeUiState.Active -> {
                    val question = state.currentQuestion

                    Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        TimerProgressBar(progress = state.timerProgress, isTimeWarning = state.remainingSeconds <= 4, modifier = Modifier.padding(bottom = 12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Question ${state.session.currentQuestionIndex + 1}/10", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = TimerProgressGold, modifier = Modifier.size(18.dp))
                                Text(text = "${state.streak}d", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TimerProgressGold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (question.promptImageUrl != null) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                modifier = Modifier.fillMaxWidth(0.85f).aspectRatio(1.6f)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(question.promptImageUrl).crossfade(true).build(),
                                    contentDescription = "Flag prompt",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = question.promptText, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            question.options.forEach { option ->
                                AnswerOptionCard(
                                    option = option,
                                    gameMode = question.gameMode,
                                    isAnswerRevealed = state.isAnswerRevealed,
                                    isSelected = state.selectedOptionId == option.id,
                                    onClick = { viewModel.onOptionSelected(option.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}