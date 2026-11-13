package de.lotto24case.presentation.ui.pullcord

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.tan

/**
 * Draws a [LampRig]: its paths, strokes and brushes are kept from frame to frame, so a swinging
 * cord allocates next to nothing. Everything is drawn in dp, scaled up to the screen's pixels.
 */
internal class LampPainter {
    private val cord = Path()
    private val shade = Path()
    private val circle = Path()
    private var shadeBuilt = false
    private val cordStroke = Stroke(PullCordDimens.CordWidth.value, cap = StrokeCap.Round)

    private var brushColors: PullCordColors? = null
    private var brushReach = -1f
    private var brushDp = -1f
    private var cone: Brush = SolidColor(Color.Transparent)
    private var coneMask: Brush = SolidColor(Color.Transparent)
    private var halo: Brush = SolidColor(Color.Transparent)
    private var sheen: Brush = SolidColor(Color.Transparent)
    private var underRim: Brush = SolidColor(Color.Transparent)
    private var spill: Brush = SolidColor(Color.Transparent)
    private var pool: Brush = SolidColor(Color.Transparent)

    private fun brushes(colors: PullCordColors, reach: Float, dp: Float) {
        if (colors == brushColors && reach == brushReach && dp == brushDp) return
        brushColors = colors
        brushReach = reach
        brushDp = dp
        cone =
            Brush.radialGradient(
                *falloff(colors.lampLight, PullCordDimens.ConeGlow) { t ->
                    (1f - t) * (1f - t) / (1f + ConeFalloff * t)
                },
                center = Offset.Zero,
                radius = reach,
            )
        val half = PullCordDimens.ConeHalfAngle
        val inner = (half - half * Feather) / Turn
        val outer = (half + half * Feather) / Turn
        coneMask =
            Brush.sweepGradient(
                *sides(Down - outer, Down - inner, Down + inner, Down + outer),
                center = Offset.Zero,
            )
        halo =
            Brush.radialGradient(
                *falloff(colors.bulbLit, PullCordDimens.HaloGlow) { t ->
                    exp(-HaloSpread * t * t) * (1f - t)
                },
                center = Offset.Zero,
                radius = PullCordDimens.HaloRadius.value * dp,
            )
        spill =
            Brush.radialGradient(
                *falloff(colors.lampLight, PullCordDimens.SpillGlow) { t ->
                    exp(-SpillSpread * t * t) * (1f - t)
                },
                center = Offset.Zero,
                radius = PullCordDimens.SpillRadius.value * dp,
            )
        pool =
            Brush.radialGradient(
                *falloff(colors.lampLight, PullCordDimens.PoolGlow) { t ->
                    exp(-PoolSpread * t * t) * (1f - t)
                },
                center = Offset.Zero,
                radius = PullCordDimens.PoolRadius.value * dp,
            )
        val rim = PullCordDimens.ShadeRim.value
        sheen =
            Brush.linearGradient(
                0.0f to Color.Transparent,
                0.22f to colors.lampShadeSheen.copy(alpha = 0.55f),
                0.38f to Color.Transparent,
                0.62f to Color.Transparent,
                0.85f to Color.Black.copy(alpha = 0.35f),
                1.0f to Color.Black.copy(alpha = 0.6f),
                start = Offset(-rim / 2f, 0f),
                end = Offset(rim / 2f, 0f),
            )
        underRim =
            Brush.linearGradient(
                0.0f to Color.Black.copy(alpha = 0.55f),
                0.5f to Color.Transparent,
                1.0f to Color.Black.copy(alpha = 0.55f),
                start = Offset(-rim / 2f, 0f),
                end = Offset(rim / 2f, 0f),
            )
    }

    fun DrawScope.clipToReveal(state: PullCordState, inside: Boolean, content: DrawScope.() -> Unit) {
        val radius = farthestCorner(state.revealFrom, size) * state.reveal.value
        circle.rewind()
        circle.addOval(Rect(state.revealFrom, radius))
        clipPath(circle, if (inside) ClipOp.Intersect else ClipOp.Difference) { content() }
    }

    fun DrawScope.drawLight(state: PullCordState, colors: PullCordColors) {
        val dp = state.density
        val rig = state.rig
        val reach = max(size.width, size.height) / dp * ConeReach
        brushes(colors, reach, dp)
        val bulb = rig.onShade(Offset(0f, RimY))
        val bulbLocal = rig.pivot + Offset(0f, RimY)
        val bulbX = bulb.x * dp
        val bulbY = bulb.y * dp
        val tilt = rig.tilt
        val shadow = shadowEdge()

        scale(dp, dp, Offset.Zero) {
            rotateRad(tilt, rig.pivot) {
                translate(bulbLocal.x, bulbLocal.y) {
                    val apexInside = PullCordDimens.ShadeHeight.value * PullCordDimens.ApexInside
                    val apexY = -apexInside
                    val halfAngle = PullCordDimens.ConeHalfAngle
                    val edgeSlope = tan(halfAngle)
                    val maskW = 2f * reach * edgeSlope + MaskMargin
                    val mask =
                        Rect(
                            -maskW / 2f,
                            apexY,
                            maskW / 2f,
                            reach,
                        )
                    clipRect(mask.left, mask.top, mask.right, mask.bottom) {
                        translate(0f, apexY) {
                            drawCircle(cone, radius = reach, center = Offset.Zero)
                            drawCircle(coneMask, radius = reach, center = Offset.Zero, blendMode = BlendMode.DstIn)
                        }
                    }
                    val sourceY = -PullCordDimens.LightSource.value
                    drawCircle(
                        Brush.sweepGradient(*shadow, center = Offset(0f, sourceY)),
                        center = Offset(0f, sourceY),
                        radius = reach,
                        blendMode = BlendMode.DstIn,
                    )
                }
            }
        }

        drawCircle(
            halo,
            radius = PullCordDimens.HaloRadius.value * dp,
            center = Offset(bulbX, bulbY),
        )
        drawCircle(
            spill,
            radius = PullCordDimens.SpillRadius.value * dp,
            center = Offset(bulbX, bulbY),
        )
        drawCircle(
            pool,
            radius = PullCordDimens.PoolRadius.value * dp,
            center = Offset(bulbX, bulbY),
        )
    }

    fun DrawScope.drawLamp(state: PullCordState, colors: PullCordColors) {
        val dp = state.density
        val rig = state.rig
        scale(dp, dp, Offset.Zero) {
            drawCanopy(rig.pivot, colors)
            drawCord(rig, colors)
            drawShade(rig, colors, state.lit)
        }
    }

    private fun DrawScope.drawCanopy(at: Offset, colors: PullCordColors) {
        val w = PullCordDimens.CanopyWidth.value
        val h = PullCordDimens.CanopyHeight.value
        drawRoundRect(
            colors.lampBrass,
            topLeft = Offset(at.x - w / 2f, at.y),
            size = Size(w, h),
            cornerRadius = CornerRadius(h / 2f),
        )
    }

    private fun DrawScope.drawCord(rig: LampRig, colors: PullCordColors) {
        val n = rig.points
        if (n < 2) return
        cord.rewind()
        val first = rig.point(0)
        cord.moveTo(first.x, first.y)
        for (i in 1 until n) {
            val p = rig.point(i)
            cord.lineTo(p.x, p.y)
        }
        drawPath(cord, colors.lampCord, style = cordStroke)
        val bead = rig.bead
        val r = PullCordDimens.BeadRadius.value
        drawCircle(colors.lampBrass, radius = r, center = bead)
        drawCircle(
            Color.White.copy(alpha = BeadGlint),
            radius = r * 0.35f,
            center = bead - Offset(r * 0.3f, r * 0.3f),
        )
    }

    private fun DrawScope.drawShade(rig: LampRig, colors: PullCordColors, lit: Boolean) {
        buildShade()
        val pivot = rig.pivot
        val tilt = rig.tilt
        rotateRad(tilt, pivot) {
            val rodTop = pivot + Offset(0f, PullCordDimens.CanopyHeight.value)
            val rodBottom = pivot + Offset(0f, PullCordDimens.Rod.value)
            drawLine(
                colors.lampBrass,
                rodTop,
                rodBottom,
                strokeWidth = RodWidth,
                cap = StrokeCap.Round,
            )
            val bulbCenter = pivot + Offset(0f, RimY)
            val bulbR = PullCordDimens.BulbRadius.value
            val bulbColor = if (lit) colors.bulbLit else colors.bulbOff
            drawCircle(bulbColor, radius = bulbR, center = bulbCenter)
            if (lit) {
                drawCircle(
                    Color.White.copy(alpha = 0.45f),
                    radius = bulbR * 0.45f,
                    center = bulbCenter,
                )
            }
            drawShadeBody(rodBottom, colors)
        }
    }

    private fun DrawScope.drawShadeBody(top: Offset, colors: PullCordColors) {
        val rim = PullCordDimens.ShadeRim.value / 2f
        val height = PullCordDimens.ShadeHeight.value
        val box = Rect(top.x - rim, top.y, top.x + rim, top.y + height)
        translate(top.x, top.y) {
            drawPath(shade, colors.lampShade)
            drawPath(shade, sheen)
        }
        drawLine(
            colors.lampBrass,
            Offset(box.left, box.bottom),
            Offset(box.right, box.bottom),
            strokeWidth = RimWidth,
            cap = StrokeCap.Round,
        )
        drawRoundRect(
            colors.lampBrass,
            topLeft = top - Offset(PullCordDimens.ShadeTop.value / 2f + 1f, CollarHeight / 2f),
            size = Size(PullCordDimens.ShadeTop.value + 2f, CollarHeight),
            cornerRadius = CornerRadius(CollarHeight / 2f),
        )
    }

    private fun buildShade() {
        if (shadeBuilt) return
        shadeBuilt = true
        val top = PullCordDimens.ShadeTop.value / 2f
        val rim = PullCordDimens.ShadeRim.value / 2f
        val h = PullCordDimens.ShadeHeight.value
        shade.moveTo(-top, 0f)
        shade.cubicTo(-top - 6f, h * 0.45f, -rim, h * 0.5f, -rim, h)
        shade.lineTo(rim, h)
        shade.cubicTo(rim, h * 0.5f, top + 6f, h * 0.45f, top, 0f)
        shade.close()
    }
}
