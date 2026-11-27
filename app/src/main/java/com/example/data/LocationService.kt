package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.domain.model.GeoPoint
import com.example.domain.model.RouteDestination
import com.example.domain.model.RouteProvider
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private data class PresetTemplate(
    val id: String,
    val label: String,
    val address: String,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val dLat: Double,
    val dLng: Double,
)

class LocationService(private val context: Context? = null) {
    private val defaultOrigin = GeoPoint(
        lat = 48.8566,
        lng = 2.3522,
        accuracyMeters = 12.0,
        timestamp = System.currentTimeMillis(),
    )

    private val presetTemplates = listOf(
        PresetTemplate("dest-home", "Maison", "12 rue Victor Hugo, Paris", 2.4, 20, 0.015, 0.019),
        PresetTemplate("dest-work", "Travail", "45 avenue de l’Opéra, Paris", 4.1, 30, 0.022, -0.016),
        PresetTemplate("dest-station", "Gare de Lyon", "Place Louis-Armand, Paris", 3.3, 25, -0.014, 0.024),
        PresetTemplate("dest-trail", "Sentier des Crêtes", "Forêt domaniale — Boucle Nord", 6.8, 90, 0.032, 0.029),
    )

    fun hasLocationPermission(): Boolean {
        val ctx = context ?: return false
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
        return coarse == PackageManager.PERMISSION_GRANTED || fine == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun getDeviceLastKnownLocationOrDefault(): GeoPoint {
        val ctx = context ?: return defaultOrigin.copy(timestamp = System.currentTimeMillis())
        if (!hasLocationPermission()) return defaultOrigin.copy(timestamp = System.currentTimeMillis())
        return try {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            val best = providers
                .mapNotNull { runCatching { lm?.getLastKnownLocation(it) }.getOrNull() }
                .maxByOrNull { it.time }
            if (best != null) {
                GeoPoint(
                    lat = best.latitude,
                    lng = best.longitude,
                    accuracyMeters = best.accuracy.toDouble().coerceAtLeast(5.0),
                    timestamp = best.time,
                )
            } else {
                defaultOrigin.copy(timestamp = System.currentTimeMillis())
            }
        } catch (_: Exception) {
            defaultOrigin.copy(timestamp = System.currentTimeMillis())
        }
    }

    fun interpolatePositionAlongRoute(
        route: RouteDestination,
        progressPercent: Int,
        deviationMeters: Double = 0.0,
        inaccurateGps: Boolean = false,
    ): GeoPoint {
        val clamped = max(0, min(100, progressPercent)) / 100.0
        val pts = route.routePoints
        if (pts.size < 2) {
            return route.origin.copy(timestamp = System.currentTimeMillis())
        }
        val scaled = clamped * (pts.size - 1)
        val idx = min(pts.size - 2, floor(scaled).toInt())
        val frac = scaled - idx
        val p1 = pts[idx]
        val p2 = pts[idx + 1]

        val devOffsetDeg = deviationMeters / 111000.0
        return GeoPoint(
            lat = p1.lat + (p2.lat - p1.lat) * frac + devOffsetDeg * 0.7,
            lng = p1.lng + (p2.lng - p1.lng) * frac - devOffsetDeg * 0.7,
            accuracyMeters = if (inaccurateGps) 260.0 else 12.0,
            timestamp = System.currentTimeMillis(),
        )
    }

    fun calculateDistanceKm(from: GeoPoint, to: GeoPoint): Double {
        val r = 6371.0
        val dLat = (to.lat - from.lat) * PI / 180.0
        val dLng = (to.lng - from.lng) * PI / 180.0
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(from.lat * PI / 180.0) * cos(to.lat * PI / 180.0) *
            sin(dLng / 2) * sin(dLng / 2)
        return r * 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
    }

    fun getPresetDestinations(origin: GeoPoint = getDeviceLastKnownLocationOrDefault()): List<RouteDestination> =
        presetTemplates.map { tpl ->
            val dest = GeoPoint(
                lat = origin.lat + tpl.dLat,
                lng = origin.lng + tpl.dLng,
                accuracyMeters = 10.0,
                timestamp = System.currentTimeMillis(),
            )
            RouteDestination(
                id = tpl.id,
                label = tpl.label,
                address = tpl.address,
                distanceKm = tpl.distanceKm,
                estimatedMinutes = tpl.estimatedMinutes,
                origin = origin,
                destination = dest,
                routePoints = buildRoutePoints(origin, dest),
                provider = RouteProvider.MOCK,
            )
        }

    fun resolveDestination(query: String, origin: GeoPoint = getDeviceLastKnownLocationOrDefault()): RouteDestination {
        val trimmed = query.trim().ifEmpty { "Maison" }
        val presets = getPresetDestinations(origin)
        val matched = presets.firstOrNull {
            it.label.equals(trimmed, ignoreCase = true) ||
                it.address.contains(trimmed, ignoreCase = true)
        }
        if (matched != null) return matched

        val hash = trimmed.fold(0) { acc, ch -> acc + ch.code }
        val distanceKm = round((1.4 + (hash % 45) / 10.0) * 10.0) / 10.0
        val estimatedMinutes = max(10, (distanceKm * 10.0).roundToInt())
        val dest = GeoPoint(
            lat = origin.lat + 0.012 + ((hash % 15) - 7) * 0.002,
            lng = origin.lng + 0.014 + (((hash shr 2) % 15) - 7) * 0.002,
            accuracyMeters = 10.0,
            timestamp = System.currentTimeMillis(),
        )
        return RouteDestination(
            id = "dest-custom-$hash",
            label = trimmed,
            address = trimmed,
            distanceKm = distanceKm,
            estimatedMinutes = estimatedMinutes,
            origin = origin,
            destination = dest,
            routePoints = buildRoutePoints(origin, dest),
            provider = RouteProvider.MOCK,
        )
    }

    private fun buildRoutePoints(origin: GeoPoint, dest: GeoPoint): List<GeoPoint> {
        val steps = 6
        return (0..steps).map { i ->
            val t = i.toDouble() / steps
            val curveOffset = sin(t * PI) * 0.0022
            GeoPoint(
                lat = origin.lat + (dest.lat - origin.lat) * t + curveOffset,
                lng = origin.lng + (dest.lng - origin.lng) * t - curveOffset * 0.6,
                accuracyMeters = 12.0,
                timestamp = origin.timestamp,
            )
        }
    }
}
