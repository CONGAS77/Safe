package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSafePalette

enum class PillTone {
    GREEN,
    ORANGE,
    RED,
    NEUTRAL,
}

@Composable
fun SafeCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    borderColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    cornerRadius: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalSafePalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor ?: palette.bgSurface,
        border = BorderStroke(1.dp, borderColor ?: palette.borderSubtle),
        shadowElevation = if (palette.isDark) 0.dp else 1.dp,
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun SafePill(
    text: String,
    tone: PillTone,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val palette = LocalSafePalette.current
    val (bg, fg, border) = when (tone) {
        PillTone.GREEN -> Triple(palette.greenSoft, palette.green600, palette.greenBorder)
        PillTone.ORANGE -> Triple(palette.orangeSoft, palette.orange600, palette.orangeBorder)
        PillTone.RED -> Triple(palette.redSoft, palette.red600, palette.redBorder)
        PillTone.NEUTRAL -> Triple(palette.bgSurfaceMuted, palette.textSecondary, palette.borderSubtle)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (leadingIcon != null) leadingIcon()
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                ),
                color = fg,
            )
        }
    }
}

@Composable
fun SafeGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush? = null,
    enabled: Boolean = true,
    minHeight: Dp = 48.dp,
    cornerRadius: Dp = 16.dp,
    testTag: String? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val palette = LocalSafePalette.current
    val activeBrush = brush ?: Brush.linearGradient(listOf(palette.green500, palette.green600))
    val mod = if (testTag != null) modifier.testTag(testTag) else modifier

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = mod
            .defaultMinSize(minHeight = minHeight)
            .alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.7f),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
                .background(activeBrush, RoundedCornerShape(cornerRadius))
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        }
    }
}

@Composable
fun SafeSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = 48.dp,
    testTag: String? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val palette = LocalSafePalette.current
    val mod = if (testTag != null) modifier.testTag(testTag) else modifier
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = mod.defaultMinSize(minHeight = minHeight),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.bgSurfaceMuted,
            contentColor = palette.textPrimary,
        ),
        border = BorderStroke(1.dp, palette.borderSubtle),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        content = content,
    )
}

@Composable
fun SafeOutlineDangerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 48.dp,
    testTag: String? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val palette = LocalSafePalette.current
    val mod = if (testTag != null) modifier.testTag(testTag) else modifier
    OutlinedButton(
        onClick = onClick,
        modifier = mod.defaultMinSize(minHeight = minHeight),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.redSoft,
            contentColor = palette.red600,
        ),
        border = BorderStroke(1.dp, palette.redBorder),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        content = content,
    )
}

@Composable
fun SafeDisclaimerBox(
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val palette = LocalSafePalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = palette.bgSurfaceMuted,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (leadingIcon != null) leadingIcon()
            Text(
                text = text,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = palette.textMuted,
            )
        }
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalSafePalette.current
    Text(
        text = text.uppercase(),
        modifier = modifier.padding(top = 18.dp, bottom = 10.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = palette.textMuted,
    )
}

@Composable
fun PulseDot(
    color: Color? = null,
    modifier: Modifier = Modifier,
) {
    val palette = LocalSafePalette.current
    val dotColor = color ?: palette.green500
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseScale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseAlpha",
    )
    Box(modifier = modifier.size(14.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(scale)
                .alpha(alpha)
                .background(dotColor, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(dotColor, CircleShape),
        )
    }
}
