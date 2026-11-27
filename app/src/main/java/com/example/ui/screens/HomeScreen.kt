package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ActivityType
import com.example.domain.model.CheckIn
import com.example.domain.model.SafeSession
import com.example.domain.model.UserProfile
import com.example.ui.components.PulseDot
import com.example.ui.components.SafeCard
import com.example.ui.components.SafeDisclaimerBox
import com.example.ui.components.SafeGradientButton
import com.example.ui.components.SafeSecondaryButton
import com.example.ui.components.SectionTitle
import com.example.ui.theme.LocalSafePalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    user: UserProfile,
    activeSession: SafeSession?,
    activeCheckIn: CheckIn?,
    t: (String) -> String,
    onOpenWizard: (ActivityType?) -> Unit,
    onOpenSessionTab: () -> Unit,
    onOpenCheckInModal: () -> Unit,
    onConfirmCheckInOk: (String) -> Unit,
) {
    val palette = LocalSafePalette.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // Greeting
        Spacer(Modifier.height(4.dp))
        Text(
            text = buildString {
                append(t("home.greeting"))
                if (user.signedIn && user.displayName.isNotBlank()) {
                    append(" ")
                    append(user.displayName)
                }
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.textSecondary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = t("home.question"),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 34.sp,
            letterSpacing = (-0.5).sp,
            color = palette.textPrimary,
        )
        Spacer(Modifier.height(18.dp))

        // Active session banner
        if (activeSession != null) {
            SafeCard(
                containerColor = palette.greenSoft,
                borderColor = palette.greenBorder,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PulseDot()
                        Column {
                            Text(
                                text = t("home.active.banner"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = palette.textPrimary,
                            )
                            Text(
                                text = "${activeSession.title} → ${activeSession.destination.label}",
                                fontSize = 12.sp,
                                color = palette.textSecondary,
                            )
                        }
                    }
                    SafeGradientButton(
                        onClick = onOpenSessionTab,
                        modifier = Modifier.padding(start = 8.dp),
                        minHeight = 40.dp,
                        cornerRadius = 12.dp,
                        testTag = "home_resume_session_button",
                    ) {
                        Text(
                            text = t("home.active.resume"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Primary Hero CTA
        SafeCard(
            contentPadding = PaddingValues(20.dp),
            cornerRadius = 24.dp,
        ) {
            SafeGradientButton(
                onClick = {
                    if (activeSession != null) onOpenSessionTab() else onOpenWizard(null)
                },
                minHeight = 76.dp,
                cornerRadius = 24.dp,
                testTag = "home_main_cta_button",
            ) {
                Text(text = "🟢", fontSize = 18.sp)
                Text(
                    text = if (activeSession != null) t("home.active.resume") else t("home.cta.main"),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = t("home.cta.hint"),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Four quick shortcuts
        SectionTitle(text = t("home.shortcuts.title"))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ShortcutCard(
                emoji = "🚶",
                title = t("home.shortcut.walk_home"),
                subtitle = t("home.shortcut.walk_home.sub"),
                modifier = Modifier.weight(1f),
                testTag = "shortcut_walk_home",
                onClick = { onOpenWizard(ActivityType.WALK_HOME) },
            )
            ShortcutCard(
                emoji = "🚗",
                title = t("home.shortcut.car"),
                subtitle = t("home.shortcut.car.sub"),
                modifier = Modifier.weight(1f),
                testTag = "shortcut_car",
                onClick = { onOpenWizard(ActivityType.CAR) },
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ShortcutCard(
                emoji = "🥾",
                title = t("home.shortcut.hike"),
                subtitle = t("home.shortcut.hike.sub"),
                modifier = Modifier.weight(1f),
                testTag = "shortcut_hike",
                onClick = { onOpenWizard(ActivityType.HIKE) },
            )
            ShortcutCard(
                emoji = "🏊",
                title = t("home.shortcut.activity"),
                subtitle = t("home.shortcut.activity.sub"),
                modifier = Modifier.weight(1f),
                testTag = "shortcut_activity",
                onClick = { onOpenWizard(ActivityType.ACTIVITY) },
            )
        }

        Spacer(Modifier.height(16.dp))

        // Independent Check-in Card
        SafeCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(palette.bgSurfaceMuted, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = t("home.checkin.title"),
                            tint = palette.green500,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column {
                        Text(
                            text = t("home.checkin.title"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = palette.textPrimary,
                        )
                        Text(
                            text = t("home.checkin.sub"),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = palette.textMuted,
                        )
                    }
                }
                Spacer(Modifier.size(8.dp))
                SafeSecondaryButton(
                    onClick = onOpenCheckInModal,
                    minHeight = 42.dp,
                    testTag = "home_schedule_checkin_button",
                ) {
                    Text(
                        text = t("home.checkin.schedule"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (activeCheckIn != null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = palette.borderSubtle)
                Spacer(Modifier.height(12.dp))
                val formattedDue = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(activeCheckIn.dueAt))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(t("home.checkin.active"))
                                append(" ")
                            }
                            append(formattedDue)
                        },
                        fontSize = 13.sp,
                        color = palette.textPrimary,
                    )
                    SafeGradientButton(
                        onClick = { onConfirmCheckInOk(activeCheckIn.id) },
                        modifier = Modifier.defaultMinSize(minWidth = 110.dp),
                        minHeight = 38.dp,
                        cornerRadius = 12.dp,
                        testTag = "home_confirm_checkin_button",
                    ) {
                        Text(
                            text = t("home.checkin.ok"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Calm reassurance footer
        SafeDisclaimerBox(
            text = t("app.disclaimer.complementary"),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = palette.green500,
                    modifier = Modifier.size(18.dp),
                )
            },
        )
    }
}

@Composable
private fun ShortcutCard(
    emoji: String,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalSafePalette.current
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 96.dp)
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = palette.bgSurface,
        border = BorderStroke(1.dp, palette.borderSubtle),
        shadowElevation = if (palette.isDark) 0.dp else 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = palette.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = palette.textMuted,
            )
        }
    }
}
