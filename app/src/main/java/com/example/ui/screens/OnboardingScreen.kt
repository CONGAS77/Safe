package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SafeDisclaimerBox
import com.example.ui.components.SafeGradientButton
import com.example.ui.components.SafeSecondaryButton
import com.example.ui.theme.LocalSafePalette

private data class OnboardingSlide(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val note: String,
)

@Composable
fun OnboardingScreen(
    t: (String) -> String,
    onComplete: () -> Unit,
) {
    val palette = LocalSafePalette.current
    var step by remember { mutableIntStateOf(0) }

    val slides = listOf(
        OnboardingSlide(
            icon = Icons.Filled.Shield,
            title = t("onboarding.1.title"),
            body = t("onboarding.1.body"),
            note = t("onboarding.1.note"),
        ),
        OnboardingSlide(
            icon = Icons.Filled.Tune,
            title = t("onboarding.2.title"),
            body = t("onboarding.2.body"),
            note = t("onboarding.2.note"),
        ),
        OnboardingSlide(
            icon = Icons.Filled.Group,
            title = t("onboarding.3.title"),
            body = t("onboarding.3.body"),
            note = t("onboarding.3.note"),
        ),
        OnboardingSlide(
            icon = Icons.Filled.Lock,
            title = t("onboarding.4.title"),
            body = t("onboarding.4.body"),
            note = t("onboarding.4.note"),
        ),
    )

    BackHandler(enabled = step > 0) {
        step -= 1
    }

    val current = slides[step]
    val isLast = step == slides.lastIndex

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = palette.bgApp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                Brush.linearGradient(listOf(palette.green500, palette.green600)),
                                RoundedCornerShape(11.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("S", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "SAFE",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.7.sp,
                            color = palette.textPrimary,
                        )
                        Text(
                            text = t("app.subtitle"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.textMuted,
                        )
                    }
                }

                if (!isLast) {
                    SafeSecondaryButton(
                        onClick = onComplete,
                        minHeight = 40.dp,
                        testTag = "onboarding_skip_button",
                    ) {
                        Text(t("onboarding.skip"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    modifier = Modifier.size(92.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = palette.greenSoft,
                    border = BorderStroke(1.dp, palette.greenBorder),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = current.icon,
                            contentDescription = current.title,
                            tint = palette.green500,
                            modifier = Modifier.size(42.dp),
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = current.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center,
                    color = palette.textPrimary,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = current.body,
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center,
                    color = palette.textSecondary,
                )
                Spacer(Modifier.height(18.dp))
                SafeDisclaimerBox(text = current.note)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    slides.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .width(if (index == step) 24.dp else 8.dp)
                                .height(8.dp)
                                .background(
                                    color = if (index == step) palette.green500 else palette.borderSubtle,
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                SafeGradientButton(
                    onClick = { if (isLast) onComplete() else step += 1 },
                    minHeight = 56.dp,
                    testTag = "onboarding_next_button",
                ) {
                    Text(
                        text = if (isLast) t("onboarding.start") else t("onboarding.next"),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
