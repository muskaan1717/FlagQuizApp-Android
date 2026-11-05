package com.flagquiz.app.presentation.stats.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flagquiz.app.core.designsystem.theme.CorrectGreen
import com.flagquiz.app.domain.model.Country

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlagCollectionGrid(
    allCountries: List<Country>,
    modifier: Modifier = Modifier
) {
    // Mastered = Encountered at least twice with 80%+ accuracy!
    val mastered = allCountries.filter { it.timesAnswered >= 2 && (it.timesCorrect.toFloat() / it.timesAnswered.toFloat()) >= 0.8f }
    val inProgress = allCountries.filter { it.timesAnswered > 0 && !mastered.contains(it) }
    val unseenCount = allCountries.size - (mastered.size + inProgress.size)

    val masteryPercent = if (allCountries.isNotEmpty()) {
        (mastered.size.toFloat() / allCountries.size.toFloat())
    } else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Flag Collection & Mastery",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { masteryPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = CorrectGreen,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BadgeCard(label = "Mastered", count = mastered.size, color = CorrectGreen, modifier = Modifier.weight(1f))
            BadgeCard(label = "Learning", count = inProgress.size, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
            BadgeCard(label = "Unseen", count = unseenCount, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mastered.isNotEmpty()) {
            Text(
                text = "Recently Mastered",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                mastered.take(18).forEach { country ->
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 30.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, CorrectGreen, RoundedCornerShape(4.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(country.flagPngUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = country.commonName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeCard(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "$count", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}