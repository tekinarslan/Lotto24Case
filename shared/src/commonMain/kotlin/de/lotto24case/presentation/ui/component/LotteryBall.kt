package de.lotto24case.presentation.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.lotto24case.presentation.model.BallUiModel
import de.lotto24case.presentation.model.BallUiType
import de.lotto24case.presentation.theme.AuraTheme
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * High-fidelity 3D Lottery Ball with drop-and-settle spring entrance and tactile press physics:
 * - 3D Spherical Rendering: Semantic highlight and shadow gradient with top-left specular illumination.
 *   Regular balls use [AuraTheme.colors.ballRegularHighlight] and [AuraTheme.colors.ballRegularShadow]
 *   to ensure crisp 3D depth in both Light and Dark modes.
 * - Single-Progress Performance Architecture: Instead of multiple Animatable coroutines, each ball uses
 *   a SINGLE Animatable progress (0f -> 1f) driven by Spring physics. Translation, rotation, squash/stretch,
 *   and alpha are cleanly derived from progress in the Draw phase via [Modifier.graphicsLayer].
 * - Settle Guarantee: Guaranteed convergence to translationY=0, scale=1, rotation=0, alpha=1.
 * - Colors derived from [AuraTheme.colors].
 */
@Composable
fun LotteryBall(
    model: BallUiModel,
    modifier: Modifier = Modifier,
    animationKey: String = "",
    size: Dp = 38.dp,
    onClick: () -> Unit = {}
) {
    val colors = AuraTheme.colors
    val density = LocalDensity.current

    // 1. Interactive Press Animation (independent from entrance animation)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BallPressScale"
    )

    // 2. Single Animation Progress (0f -> 1f) with Spring Physics
    // Eliminates multiple coroutines and animatables for silky smooth 60/120fps iOS & Android performance
    val entranceProgress = remember { Animatable(0f) }

    val resolvedKey = animationKey.ifEmpty { "${model.type}_${model.number}_${model.index}" }

    LaunchedEffect(resolvedKey) {
        val staggerDelayMs = (model.index * 50L).coerceAtLeast(0L)
        entranceProgress.snapTo(0f)

        if (staggerDelayMs > 0) {
            delay(staggerDelayMs)
        }

        entranceProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    // 3. 3D Color & Sphere Highlight Tokens from Theme
    val (baseColor, highlightColor, shadowColor, textColor, borderColor, ambientShadow) = when (model.type) {
        BallUiType.REGULAR -> {
            Tuple6(
                colors.ballRegularBackground,
                colors.ballRegularHighlight,
                colors.ballRegularShadow,
                colors.ballRegularText,
                colors.ballRegularBorder,
                Color.Transparent // Keep flat/border on normal balls for lightweight rendering on iOS
            )
        }
        BallUiType.SUPER_ZAHL -> {
            Tuple6(
                colors.superzahlRuby,
                colors.superzahlRubyHighlight,
                colors.superzahlRubyShadow,
                Color.White,
                Color.Transparent,
                colors.superzahlRuby.copy(alpha = 0.25f)
            )
        }
        BallUiType.EURO_ZAHL -> {
            Tuple6(
                colors.eurozahlAmber,
                colors.eurozahlAmberHighlight,
                colors.eurozahlAmberShadow,
                Color.White,
                Color.Transparent,
                colors.eurozahlAmber.copy(alpha = 0.30f)
            )
        }
    }

    val typeLabel = when (model.type) {
        BallUiType.REGULAR -> "Zahl"
        BallUiType.SUPER_ZAHL -> "Superzahl"
        BallUiType.EURO_ZAHL -> "Eurozahl"
    }

    // Pre-allocate immutable color list once outside draw phase
    val gradientColors = remember(highlightColor, baseColor, shadowColor) {
        listOf(highlightColor, baseColor, shadowColor)
    }

    val initialDropPx = with(density) { -50.dp.toPx() }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                val p = entranceProgress.value

                // Derived translation: starts at initialDropPx, strictly settles to 0f
                translationY = initialDropPx * (1f - p)

                // Derived rotation: subtle initial tilt that settles to 0f
                val rotationBase = if (model.number % 2 == 0) -15f else 15f
                rotationZ = rotationBase * (1f - p)

                // Derived impact squash & stretch: clean settle
                val impactPhase = ((p - 0.75f) / 0.25f).coerceIn(0f, 1f)
                val squashAmount = if (p in 0.75f..1f) sin(impactPhase * kotlin.math.PI.toFloat()) * 0.05f else 0f

                scaleX = pressScale * (1f + squashAmount)
                scaleY = pressScale * (1f - squashAmount)

                // Derived alpha: smooth fade in over first 40% of progress
                alpha = (p * 2.5f).coerceIn(0.15f, 1f)
            }
            .then(
                if (ambientShadow != Color.Transparent) {
                    Modifier.shadow(
                        elevation = 2.dp,
                        shape = CircleShape,
                        ambientColor = ambientShadow,
                        spotColor = ambientShadow
                    )
                } else Modifier
            )
            .clip(CircleShape)
            .drawWithCache {
                val canvasSize = this.size
                val radius = canvasSize.minDimension / 2f
                val lightOffset = Offset(radius * 0.7f, radius * 0.6f)
                val radialBrush = Brush.radialGradient(
                    colors = gradientColors,
                    center = lightOffset,
                    radius = radius * 1.35f
                )
                val strokeStyle = if (borderColor != Color.Transparent) Stroke(width = 1.dp.toPx()) else null

                onDrawBehind {
                    // Cached radial brush drawing without per-frame allocations
                    drawCircle(brush = radialBrush)

                    if (strokeStyle != null) {
                        drawCircle(
                            color = borderColor,
                            radius = radius - 0.5f,
                            style = strokeStyle
                        )
                    }
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                contentDescription = "$typeLabel ${model.number}"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = model.number.toString(),
            color = textColor,
            fontSize = if (model.number >= 10) 14.sp else 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)
