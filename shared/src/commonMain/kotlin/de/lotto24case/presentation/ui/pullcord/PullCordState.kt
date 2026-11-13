package de.lotto24case.presentation.ui.pullcord

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * A pull-cord lamp and the screen it lights: [lit] or not.
 */
@Stable
class PullCordState internal constructor(lit: Boolean, private val scope: CoroutineScope) {
    var lit: Boolean by mutableStateOf(lit)
        private set

    internal val rig = LampRig(Offset.Zero) { toggle() }

    internal var density = 1f
        private set

    internal var frame by mutableIntStateOf(0)
        private set

    private var hung by mutableIntStateOf(0)

    internal var awake by mutableStateOf(false)
        private set

    internal var revealing by mutableStateOf(false)
        private set

    internal val reveal = Animatable(1f)

    internal var revealTop by mutableStateOf(lit)
        private set

    internal var revealFrom = Offset.Zero
        private set

    internal val brightness = Animatable(if (lit) 1f else 0f)

    private var offFrom = 1f

    internal val glow: Float
        get() {
            if (lit || !revealing) return brightness.value
            val left = if (revealTop) reveal.value else 1f - reveal.value
            return offFrom * left
        }

    internal var onSwitch: ((Boolean) -> Unit)? = null

    private var revealJob: Job? = null
    private var brightnessJob: Job? = null

    fun grab(at: Offset): Boolean {
        val taken = rig.grab(at / density)
        if (taken) awake = true
        return taken
    }

    fun dragTo(at: Offset) {
        if (!rig.held) return
        rig.dragTo(at / density)
        awake = true
    }

    fun release() {
        rig.release()
        awake = true
    }

    fun toggle() {
        val next = !lit
        lit = next
        revealJob?.cancel()
        if (revealing) {
            val wasFrom = bulb()
            val wasCenter = (wasFrom - revealFrom).getDistance()
            val wasReach = farthestCorner(revealFrom, Size(width.toFloat(), Float.MAX_VALUE))
            val wasCover = wasReach * reveal.value
            val currentCover = wasCover - wasCenter
            val newReach = farthestCorner(wasFrom, Size(width.toFloat(), Float.MAX_VALUE))
            val startFrom = (currentCover / newReach).coerceIn(0f, 1f)
            revealFrom = wasFrom
            revealTop = !revealTop
            revealJob =
                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    reveal.snapTo(startFrom)
                    reveal.animateTo(
                        1f,
                        tween(
                            (PullCordDimens.RevealMs * (1f - startFrom)).toInt(),
                            easing = FastOutSlowInEasing,
                        ),
                    )
                    revealing = false
                }
        } else {
            revealFrom = bulb()
            revealTop = lit
            revealing = true
            revealJob =
                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    reveal.snapTo(0f)
                    reveal.animateTo(
                        1f,
                        tween(PullCordDimens.RevealMs, easing = FastOutSlowInEasing),
                    )
                    revealing = false
                }
        }
        if (!lit) offFrom = brightness.value
        brightnessJob?.cancel()
        brightnessJob = scope.launch {
            if (lit) {
                brightness.animateTo(
                    1f,
                    tween(PullCordDimens.WarmUpMs, easing = FastOutSlowInEasing),
                )
            } else {
                brightness.animateTo(0f, tween(PullCordDimens.OffMs))
            }
        }
        onSwitch?.invoke(lit)
    }

    fun stir() {
        rig.stir(PullCordDimens.DraughtSpeed)
        awake = true
        frame++
    }

    fun snapTo(lit: Boolean) {
        revealJob?.cancel()
        brightnessJob?.cancel()
        this.lit = lit
        revealTop = lit
        revealing = false
        revealJob =
            scope.launch(start = CoroutineStart.UNDISPATCHED) {
                reveal.snapTo(1f)
                brightness.snapTo(if (lit) 1f else 0f)
            }
    }

    private var width = 0
    private var across = 0.5f

    internal fun place(width: Int, density: Float) {
        this.width = width
        this.density = density
        hang()
    }

    internal fun hangAcross(across: Float) {
        if (!across.isFinite()) return
        val share = across.coerceIn(0f, 1f)
        if (share == this.across) return
        this.across = share
        hang()
    }

    private fun hang() {
        rig.moveTo(Offset(width * across / density, 0f))
        if (revealing) revealFrom = bulb()
        frame++
        hung++
    }

    internal fun advance(seconds: Float) {
        rig.advance(seconds)
        frame++
        if (rig.atRest) awake = false
    }

    internal fun bulb(): Offset =
        rig.onShade(Offset(0f, PullCordDimens.Rod.value + PullCordDimens.ShadeHeight.value)) *
            density

    internal val bead: Offset
        get() {
            frame
            return rig.bead * density
        }

    internal val restingBead: Offset
        get() {
            hung
            return rig.restingBead * density
        }

    internal companion object {
        fun saver(scope: CoroutineScope): Saver<PullCordState, Boolean> =
            Saver(save = { it.lit }, restore = { PullCordState(it, scope) })
    }
}

@Composable
fun rememberPullCordState(lit: Boolean = false): PullCordState {
    val scope = rememberCoroutineScope()
    return rememberSaveable(saver = PullCordState.saver(scope)) { PullCordState(lit, scope) }
}
