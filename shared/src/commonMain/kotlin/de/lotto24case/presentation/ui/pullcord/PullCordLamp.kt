package de.lotto24case.presentation.ui.pullcord

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first

@Composable
fun PullCordLamp(
    state: PullCordState,
    modifier: Modifier = Modifier,
    across: Float = 0.5f,
    colors: PullCordColors = remember { PullCordColors() },
    onSwitch: (lit: Boolean) -> Unit = {},
    content: @Composable (lit: Boolean) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val currentOnSwitch by rememberUpdatedState(onSwitch)
    DisposableEffect(state, haptics) {
        val hook: (Boolean) -> Unit = { lit ->
            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
            currentOnSwitch(lit)
        }
        state.onSwitch = hook
        onDispose { if (state.onSwitch === hook) state.onSwitch = null }
    }

    LaunchedEffect(state) {
        while (true) {
            snapshotFlow { state.awake }.first { it }
            var last = withFrameNanos { it }
            while (state.awake) {
                withFrameNanos { now ->
                    val frameSeconds =
                        ((now - last) / 1_000_000_000f).coerceAtMost(PullCordDimens.MostFrameSeconds)
                    last = now
                    state.advance(frameSeconds)
                }
            }
        }
    }

    val density = LocalDensity.current.density
    Box(
        modifier
            .onSizeChanged { size ->
                state.place(size.width, density)
                state.hangAcross(across)
            }
            .pointerInput(state) {
                awaitEachGesture {
                    val down = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.changedToDown() } ?: return@awaitEachGesture
                    if (!state.grab(down.position)) return@awaitEachGesture
                    down.consume()
                    try {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            change.consume()
                            state.dragTo(change.position)
                            if (!change.pressed) break
                        }
                    } finally {
                        state.release()
                    }
                }
            },
    ) {
        val lit = state.lit
        val painter = remember { LampPainter() }

        Box(Modifier.fillMaxSize()) {
            content(lit)
        }

        if (lit) {
            Spacer(
                Modifier.fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        blendMode = BlendMode.Plus
                        alpha = state.glow
                    }
                    .drawBehind {
                        state.frame
                        with(painter) { drawLight(state, colors) }
                    },
            )
        }

        Spacer(
            Modifier.fillMaxSize().graphicsLayer().drawBehind {
                state.frame
                with(painter) { drawLamp(state, colors) }
            },
        )

        Spacer(
            Modifier.offset {
                    val at = state.restingBead
                    val half = PullCordDimens.GrabRadius.toPx()
                    IntOffset((at.x - half).roundToInt(), (at.y - half).roundToInt())
                }
                .size(PullCordDimens.GrabRadius * 2)
                .semantics {
                    contentDescription = "Light pull cord"
                    role = Role.Switch
                    toggleableState = ToggleableState(lit)
                    stateDescription = if (lit) "On" else "Off"
                    onClick {
                        state.toggle()
                        true
                    }
                },
        )
    }
}

private fun Modifier.inert(): Modifier =
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
            }
        }
    }

private const val LiveLook = "live"
private const val GoingLook = "going"
