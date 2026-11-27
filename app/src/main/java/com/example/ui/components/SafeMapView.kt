package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GeoPoint
import com.example.domain.model.RouteDestination
import com.example.domain.model.RouteProvider
import com.example.ui.theme.LocalSafePalette
import kotlin.math.max

@Composable
fun SafeMapView(
    destination: RouteDestination,
    currentPosition: GeoPoint?,
    progressPercent: Int,
    deviationMeters: Double = 0.0,
    inaccurateGps: Boolean = false,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val palette = LocalSafePalette.current
    val pts = destination.routePoints
    val allPts = if (currentPosition != null) pts + currentPosition else pts

    val lats = allPts.map { it.lat }
    val lngs = allPts.map { it.lng }
    val minLat = (lats.minOrNull() ?: 48.85) - 0.004
    val maxLat = (lats.maxOrNull() ?: 48.87) + 0.004
    val minLng = (lngs.minOrNull() ?: 2.34) - 0.004
    val maxLng = (lngs.maxOrNull() ?: 2.37) + 0.004

    val isDeviating = deviationMeters > 250.0

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = palette.bgSurface,
        border = BorderStroke(1.dp, palette.borderSubtle),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 136.dp else 180.dp)
                    .background(palette.bgSurface),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Subtle radial highlight
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palette.green500.copy(alpha = 0.10f),
                                Color.Transparent,
                            ),
                            center = Offset(w * 0.22f, h * 0.22f),
                            radius = w * 0.65f,
                        ),
                    )

                    // Grid lines
                    val stepPx = 28.dp.toPx()
                    val gridColor = Color(0xFF94A3B8).copy(alpha = 0.14f)
                    var gx = 0f
                    while (gx <= w) {
                        drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1.dp.toPx())
                        gx += stepPx
                    }
                    var gy = 0f
                    while (gy <= h) {
                        drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1.dp.toPx())
                        gy += stepPx
                    }

                    fun project(p: GeoPoint): Offset {
                        val x = (((p.lng - minLng) / max(0.0001, maxLng - minLng)) * (w * 0.84f) + w * 0.08f).toFloat()
                        val y = (h * 0.84f - ((p.lat - minLat) / max(0.0001, maxLat - minLat)) * (h * 0.64f)).toFloat()
                        return Offset(x, y)
                    }

                    val projected = pts.map(::project)
                    if (projected.isNotEmpty()) {
                        val routePath = Path().apply {
                            moveTo(projected.first().x, projected.first().y)
                            projected.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = routePath,
                            color = Color(0xFF94A3B8),
                            style = Stroke(
                                width = 4.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f),
                            ),
                        )
                    }

                    val startPx = projected.firstOrNull() ?: Offset(w * 0.15f, h * 0.72f)
                    val endPx = projected.lastOrNull() ?: Offset(w * 0.85f, h * 0.28f)
                    val curPx = currentPosition?.let(::project) ?: startPx

                    // Progress line
                    val activeColor = if (isDeviating) Color(0xFFF59E0B) else Color(0xFF10B981)
                    drawLine(
                        color = activeColor,
                        start = startPx,
                        end = curPx,
                        strokeWidth = 4.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )

                    // GPS uncertainty halo
                    val haloRadius = (if (inaccurateGps) 26.dp else 13.dp).toPx()
                    drawCircle(
                        color = if (isDeviating) Color(0x38F59E0B) else Color(0x3310B981),
                        radius = haloRadius,
                        center = curPx,
                    )

                    // Origin pin
                    drawCircle(color = Color(0xFF0F172A), radius = 6.dp.toPx(), center = startPx)
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = startPx,
                        style = Stroke(width = 2.dp.toPx()),
                    )

                    // Destination pin
                    drawCircle(color = Color(0xFF059669), radius = 7.dp.toPx(), center = endPx)
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = endPx,
                        style = Stroke(width = 2.dp.toPx()),
                    )

                    // Current position marker
                    drawCircle(
                        color = if (isDeviating) Color(0xFFD97706) else Color(0xFF10B981),
                        radius = 7.dp.toPx(),
                        center = curPx,
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = curPx,
                        style = Stroke(width = 2.5.dp.toPx()),
                    )
                }

                // Top overlay pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SafePill(
                        text = if (destination.provider == RouteProvider.GOOGLE_MAPS) {
                            "Google Maps"
                        } else {
                            "Carte simulée (MVP)"
                        },
                        tone = PillTone.NEUTRAL,
                    )
                    SafePill(
                        text = "$progressPercent% · ${destination.distanceKm} km",
                        tone = PillTone.GREEN,
                    )
                }
            }

            HorizontalDivider(color = palette.borderSubtle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Départ : Position actuelle",
                    fontSize = 12.sp,
                    color = palette.textSecondary,
                )
                Text(
                    text = "→ ${destination.label}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                )
            }
        }
    }
}
