package com.flagquiz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flagquiz.app.core.designsystem.theme.TimerProgressGold
import com.flagquiz.app.data.local.preferences.UserPreferencesDataStore
import com.flagquiz.app.presentation.daily.DailyChallengeScreen
import com.flagquiz.app.presentation.game.GameScreen
import com.flagquiz.app.presentation.home.components.GameSetupBottomSheet
import com.flagquiz.app.presentation.practice.PracticeScreen
import com.flagquiz.app.presentation.results.ResultsScreen
import com.flagquiz.app.presentation.settings.SettingsScreen
import com.flagquiz.app.presentation.stats.StatsScreen
import com.flagquiz.app.ui.theme.FlagQuizAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferences: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // DYNAMIC THEME: Observes your selection in Settings in real time!
            val themeMode by preferences.themeMode.collectAsState(initial = "SYSTEM")
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            FlagQuizAppTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("HOME") }

                    var activeMode by remember { mutableStateOf("FLAG_TO_NAME") }
                    var activeDifficulty by remember { mutableStateOf("MEDIUM") }
                    var activeRegion by remember { mutableStateOf("ALL") }

                    var finalScore by remember { mutableIntStateOf(0) }
                    var totalQuestions by remember { mutableIntStateOf(10) }
                    var bestStreak by remember { mutableIntStateOf(0) }
                    var historyId by remember { mutableLongStateOf(0L) }

                    val setupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    val coroutineScope = rememberCoroutineScope()
                    var showSetupSheet by remember { mutableStateOf(false) }

                    if (showSetupSheet) {
                        GameSetupBottomSheet(
                            sheetState = setupSheetState,
                            onDismissRequest = { showSetupSheet = false },
                            onStartGame = { mode, diff, reg ->
                                coroutineScope.launch {
                                    setupSheetState.hide()
                                    showSetupSheet = false
                                    activeMode = mode.name
                                    activeDifficulty = diff.name
                                    activeRegion = reg.name
                                    currentScreen = "GAME"
                                }
                            }
                        )
                    }

                    when (currentScreen) {
                        "HOME" -> {
                            HomeScreen(
                                onStartQuickQuiz = {
                                    activeMode = "FLAG_TO_NAME"
                                    activeDifficulty = "MEDIUM"
                                    activeRegion = "ALL"
                                    currentScreen = "GAME"
                                },
                                onOpenGameModes = { showSetupSheet = true },
                                onOpenDaily = { currentScreen = "DAILY" },
                                onOpenEncyclopedia = { currentScreen = "PRACTICE" },
                                onOpenStats = { currentScreen = "STATS" },
                                onOpenSettings = { currentScreen = "SETTINGS" }
                            )
                        }
                        "DAILY" -> {
                            DailyChallengeScreen(onNavigateBack = { currentScreen = "HOME" })
                        }
                        "PRACTICE" -> {
                            PracticeScreen(onNavigateBack = { currentScreen = "HOME" })
                        }
                        "STATS" -> {
                            StatsScreen(onNavigateBack = { currentScreen = "HOME" })
                        }
                        "SETTINGS" -> {
                            SettingsScreen(onNavigateBack = { currentScreen = "HOME" })
                        }
                        "GAME" -> {
                            GameScreen(
                                mode = activeMode,
                                difficulty = activeDifficulty,
                                region = activeRegion,
                                onNavigateToResults = { score, total, streak, id ->
                                    finalScore = score
                                    totalQuestions = total
                                    bestStreak = streak
                                    historyId = id
                                    currentScreen = "RESULTS"
                                },
                                onNavigateBack = { currentScreen = "HOME" }
                            )
                        }
                        "RESULTS" -> {
                            ResultsScreen(
                                score = finalScore,
                                totalQuestions = totalQuestions,
                                streak = bestStreak,
                                historyId = historyId,
                                onPlayAgain = { currentScreen = "GAME" },
                                onNavigateHome = { currentScreen = "HOME" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onStartQuickQuiz: () -> Unit,
    onOpenGameModes: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenEncyclopedia: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Flag Quiz Master",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Test your knowledge across 195+ nations",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 1. QUICK PLAY
        Button(
            onClick = onStartQuickQuiz,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Quick Play (Marathon)", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. DAILY CHALLENGE TILE (STREAK SYSTEM)
        Card(
            onClick = onOpenDaily,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Daily Challenge (10 Flags)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Text("🔥", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. GAME MODES
        OutlinedButton(
            onClick = onOpenGameModes,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Game Modes & Continents", fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. ENCYCLOPEDIA
        OutlinedButton(
            onClick = onOpenEncyclopedia,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Flag Encyclopedia (195 Nations)", fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. STATS
        OutlinedButton(
            onClick = onOpenStats,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "High Scores & Stats", fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6. SETTINGS
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Settings", fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}