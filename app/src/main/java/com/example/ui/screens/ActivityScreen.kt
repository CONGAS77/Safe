package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PrivacySettings
import com.example.domain.model.SessionHistoryItem
import com.example.domain.model.SessionState
import com.example.ui.components.PillTone
import com.example.ui.components.SafeCard
import com.example.ui.components.SafeDisclaimerBox
import com.example.ui.components.SafePill
import com.example.ui.theme.LocalSafePalette
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun formatDayGroup(ts: Long): String {
    val calNow = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val calItem = Calendar.getInstance().apply {
        timeInMillis = ts
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffDays = ((calNow.timeInMillis - calItem.timeInMillis) / (24L * 60 * 60_000L)).toInt()
    return when (diffDays) {
        0 -> "Aujourd’hui"
        1 -> "Hier"
        else -> SimpleDateFormat("d MMMM", Locale.FRANCE).format(Date(ts))
    }
}

private fun formatDuration(mins: Int): String {
    if (mins < 60) return "$mins min"
    val h = mins / 60
    val m = mins % 60
    return "$h h ${m.toString().padStart(2, '0')}"
}

@Composable
fun ActivityScreen(
    history: List<SessionHistoryItem>,
    privacy: PrivacySettings,
    t: (String) -> String,
) {
    val palette = LocalSafePalette.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = t("history.title"),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = palette.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        SafeDisclaimerBox(
            text = "Politique de conservation active : ${privacy.retentionPolicy.value}. Les coordonnées détaillées ne sont conservées que selon vos paramètres de confidentialité.",
        )
        Spacer(Modifier.height(14.dp))

        if (history.isEmpty()) {
            SafeCard(contentPadding = PaddingValues(28.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = t("history.empty"),
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Vos sessions terminées apparaîtront ici si la conservation est activée.",
                        fontSize = 14.sp,
                        color = palette.textSecondary,
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                history.forEach { item ->
                    SafeCard {
                        Text(
                            text = formatDayGroup(item.endedAt),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textMuted,
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.activityType.emoji} ${item.title}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = palette.textPrimary,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${item.destinationLabel} · ${formatDuration(item.durationMinutes)}",
                                    fontSize = 13.sp,
                                    color = palette.textSecondary,
                                )
                            }
                            val (tone, label) = when (item.endState) {
                                SessionState.COMPLETED -> PillTone.GREEN to t("history.normalEnd")
                                SessionState.ESCALATED -> PillTone.RED to item.endState.name
                                else -> PillTone.NEUTRAL to item.endState.name
                            }
                            SafePill(text = label, tone = tone)
                        }
                    }
                }
            }
        }
    }
}
