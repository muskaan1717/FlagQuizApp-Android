package com.flagquiz.app.presentation.game

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flagquiz.app.core.designsystem.theme.TimerProgressGold
import com.flagquiz.app.presentation.game.components.AnswerOptionCard
import com.flagquiz.app.presentation.game.components.LivesRow
import com.flagquiz.app.presentation.game.components.TimerProgressBar
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    mode: String,
    difficulty: String,
    region: String,
    onNavigateToResults: (score: Int, total: Int, streak: Int, historyId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    var showQuitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showQuitDialog = true
    }

    // Tells ViewModel which mode and region to play whenever this screen opens!
    LaunchedEffect(mode, difficulty, region) {
        viewModel.initializeGame(mode, difficulty, region)
    }

    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is GameUiEffect.TriggerHapticSuccess -> {
                    view.performHapticFeedback(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            HapticFeedbackConstants.CONFIRM
                        } else {
                            HapticFeedbackConstants.KEYBOARD_TAP
                        }
                    )
                }
                is GameUiEffect.TriggerHapticFailure -> {
                    view.performHapticFeedback(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            HapticFeedbackConstants.REJECT
                        } else {
                            HapticFeedbackConstants.LONG_PRESS
                        }
                    )
                }
                is GameUiEffect.PlaySoundCorrect -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                }
                is GameUiEffect.PlaySoundWrong -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
                }
                is GameUiEffect.NavigateToResults -> {
                    onNavigateToResults(effect.score, effect.total, effect.streak, effect.historyId)
                }
            }
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Quit Quiz?") },
            text = { Text("Your score and active streak will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showQuitDialog = false
                    onNavigateBack()
                }) {
                    Text("Quit", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("Continue")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is GameUiState.Active) {
                        val session = (uiState as GameUiState.Active).session
                        Text(
                            text = "Question ${session.currentQuestionIndex + 1}/${session.totalQuestions}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showQuitDialog = true }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Quiz")
                    }
                },
                actions = {
                    if (uiState is GameUiState.Active) {
                        val state = uiState as GameUiState.Active
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            if (state.session.currentStreak > 1) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = TimerProgressGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${state.session.currentStreak}x",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TimerProgressGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            LivesRow(
                                totalLives = 3,
                                remainingLives = state.session.livesRemaining
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is GameUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Assembling Geography Questions...")
                    }
                }

                is GameUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.initializeGame(mode, difficulty, region) }) {
                            Text("Retry")
                        }
                    }
                }

                is GameUiState.Active -> {
                    val question = state.currentQuestion
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimerProgressBar(
                            progress = state.timerProgress,
                            isTimeWarning = state.isTimeWarning,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Score: ${state.session.score}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${state.remainingSeconds}s",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (state.isTimeWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Only show prompt image if the question has one!
                        // (Country-to-Flag mode sets this to null because the 4 OPTIONS are the images!)
                        if (question.promptImageUrl != null) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .aspectRatio(1.6f)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(question.promptImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Flag prompt",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(
                            text = question.promptText,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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

                        Spacer(modifier = Modifier.height(16.dp))

                        AnimatedVisibility(
                            visible = state.isAnswerRevealed,
                            enter = fadeIn() + slideInVertically { it / 2 }
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = question.funFact,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.onUseFiftyFiftyHint() },
                                enabled = !state.isAnswerRevealed && state.session.hintsFiftyFiftyRemaining > 0 && !state.isFiftyFiftyActive,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("50/50 (${state.session.hintsFiftyFiftyRemaining})")
                            }

                            OutlinedButton(
                                onClick = { viewModel.onUseSkipHint() },
                                enabled = !state.isAnswerRevealed && state.session.hintsSkipRemaining > 0,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Skip (${state.session.hintsSkipRemaining})")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                is GameUiState.Completed -> {}
            }
        }
    }
}