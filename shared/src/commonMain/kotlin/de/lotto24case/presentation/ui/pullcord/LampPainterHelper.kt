package de.lotto24case.presentation.ui.pullcord

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.atan2

internal val RimY = PullCordDimens.Rod.value + PullCordDimens.ShadeHeight.value
internal const val Feather = 0.8f
internal const val Turn = 2f * PI.toFloat()
internal const val Down = 0.25f
internal const val MaskMargin = 4f
internal const val Smoothness = 16
internal const val ConeFalloff = 5f
internal const val HaloSpread = 4f
internal const val SpillSpread = 2.5f
internal const val PoolSpread = 3f

internal inline fun falloff(
    color: Color,
    peak: Float,
    f: (Float) -> Float,
): Array<Pair<Float, Color>> =
    Array(Smoothness + 1) { i ->
        val t = i / Smoothness.toFloat()
        t to color.copy(alpha = peak * f(t))
    }

internal fun sides(from: Float, full: Float, fade: Float, to: Float): Array<Pair<Float, Color>> {
    val ease = Smoothness / 2
    val stops = ArrayList<Pair<Float, Color>>()
    stops += 0f to Color.Transparent
    for (i in 0..ease) {
        val t = i / ease.toFloat()
        stops += (from + (full - from) * t) to Color.Black.copy(alpha = t * t * (3f - 2f * t))
    }
    for (i in 0..ease) {
        val t = i / ease.toFloat()
        stops += (fade + (to - fade) * t) to Color.Black.copy(alpha = 1f - t * t * (3f - 2f * t))
    }
    stops += 1f to Color.Transparent
    return stops.toTypedArray()
}

private val EdgeThroughRim =
    (atan2(PullCordDimens.LightSource.value, PullCordDimens.ShadeRim.value / 2f) / Turn)
private const val EdgeSoftness = 0.04f
private val EdgeStart = EdgeThroughRim - EdgeSoftness
private val EdgeFull = EdgeThroughRim + EdgeSoftness

internal fun shadowEdge(): Array<Pair<Float, Color>> {
    val span = EdgeFull - EdgeStart
    fun lit(turn: Float): Float {
        val right = if (turn > 0.5f) turn - 1f else turn
        val fromRight = (right - EdgeStart) / span
        val fromLeft = (0.5f - EdgeStart - turn) / span
        val t = minOf(fromRight, fromLeft).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
    return Array(EdgeStops + 1) { i ->
        val turn = i / EdgeStops.toFloat()
        turn to Color.Black.copy(alpha = lit(turn))
    }
}

internal const val EdgeStops = 240
internal const val ConeReach = 1.4f
internal const val RodWidth = 2f
internal const val RimWidth = 2f
internal const val CollarHeight = 6f
internal const val BeadGlint = 0.55f
