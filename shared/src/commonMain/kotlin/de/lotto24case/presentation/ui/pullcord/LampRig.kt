package de.lotto24case.presentation.ui.pullcord

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.lerp
import de.lotto24case.presentation.ui.pullcord.physics.Pendulum
import de.lotto24case.presentation.ui.pullcord.physics.VerletRope
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * The lamp's moving parts, in dp: a shade hanging as a pendulum from [pivot] on the ceiling, and a
 * cord hanging from its side with a bead on the end.
 */
internal class LampRig(pivot: Offset, private val onClick: () -> Unit) {
    var pivot: Offset = pivot
        private set

    private val length = PullCordDimens.CordLength.value
    private val rope =
        VerletRope(
            points = PullCordDimens.CordPoints,
            segment = length / (PullCordDimens.CordPoints - 1),
            gravity = PullCordDimens.Gravity,
            damping = PullCordDimens.CordDamping,
            passes = PullCordDimens.ConstraintPasses,
            stillStep = PullCordDimens.StillStep,
        ).apply { hang(attachment(0f)) }

    private val shade =
        Pendulum(PullCordDimens.ShadeSwingHz, PullCordDimens.ShadeDamping, PullCordDimens.MostTilt)

    val tilt: Float
        get() = shade.angle

    private var carried = 0f
    private var quiet = 0

    private var finger = Offset.Unspecified
    private var stepFinger = Offset.Unspecified
    private var grabbedAt = Offset.Zero
    private var hold = Offset.Zero
    private var clicked = false
    private var lettingGo = false

    val held: Boolean
        get() = finger.isSpecified && !lettingGo

    var atRest = true
        private set

    val bead: Offset
        get() = rope[rope.size - 1]

    val restingBead: Offset
        get() = attachment(0f) + Offset(0f, length)

    val points: Int
        get() = rope.size

    fun point(i: Int): Offset = rope[i]

    fun onShade(local: Offset): Offset = pivot + rotate(local, tilt)

    fun grab(at: Offset): Boolean {
        if (held) return false
        if ((at - bead).getDistance() > PullCordDimens.GrabRadius.value) return false
        finger = at
        stepFinger = at
        lettingGo = false
        grabbedAt = at
        hold = bead - at
        clicked = false
        atRest = false
        quiet = 0
        return true
    }

    fun dragTo(at: Offset) {
        if (!held) return
        finger = at
        atRest = false
        quiet = 0
        val down = at.y - grabbedAt.y
        val along = abs(at.x - grabbedAt.x)
        if (!clicked && down >= PullCordDimens.ClickPull.value && down >= 2f * along) {
            clicked = true
            onClick()
        }
    }

    fun release() {
        if (!held) return
        if (finger == stepFinger) letGo() else lettingGo = true
    }

    private fun letGo() {
        finger = Offset.Unspecified
        stepFinger = Offset.Unspecified
        lettingGo = false
        rope.letGo()
    }

    fun moveTo(to: Offset) {
        val by = to - pivot
        pivot = to
        rope.shift(by)
    }

    fun stir(speed: Float) {
        if (finger.isSpecified) return
        rope.push(speed, PullCordDimens.StepSeconds)
        shade.swing += speed * PullCordDimens.ShadeStir
        atRest = false
        quiet = 0
    }

    fun advance(seconds: Float) {
        if (atRest) return
        carried += seconds
        val dt = PullCordDimens.StepSeconds
        var steps = 0
        while (carried >= dt) {
            carried -= dt
            steps++
        }
        for (k in 1..steps) {
            step(dt, k / steps.toFloat())
            val level =
                !finger.isSpecified &&
                    rope.still &&
                    rope.stretch == 1f &&
                    shade.isStill(PullCordDimens.LevelTilt, PullCordDimens.LevelSwing)
            quiet = if (level) quiet + 1 else 0
        }
        if (steps > 0 && finger.isSpecified) {
            stepFinger = finger
            if (lettingGo) letGo()
        }
        atRest = quiet >= PullCordDimens.QuietSteps
        if (atRest) carried = 0f
    }

    private fun step(dt: Float, share: Float) {
        if (finger.isSpecified) {
            val top = rope[0]
            val wanted = lerp(stepFinger, finger, share) + hold - top
            val reach = wanted.getDistance()
            val most = PullCordDimens.MostStretch.value
            val over = reach - length
            if (over > 0f) {
                val give = most * (1f - exp(-over / most))
                rope.hold(top + wanted * ((length + give) / reach), (length + give) / length)
            } else {
                rope.hold(top + wanted, 1f)
            }
        } else if (rope.stretch != 1f) {
            rope.stretch = 1f + (rope.stretch - 1f) * exp(-dt / PullCordDimens.Recoil)
            if (rope.stretch - 1f < 1e-4f) rope.stretch = 1f
        }
        swingShade(dt)
        rope.pin(attachment(tilt))
        rope.step(dt)
    }

    private fun swingShade(dt: Float) {
        val lever = rope[0] - pivot
        val along = rope[1] - rope[0]
        val reach = along.getDistance()
        val pull = (rope.stretch - 1f) * length
        val turn =
            if (reach > 0f && pull > 0f) {
                (lever.x * along.y - lever.y * along.x) / reach * pull * PullCordDimens.TiltPerPull
            } else {
                0f
            }
        shade.step(dt, turn)
    }

    private fun attachment(angle: Float): Offset =
        pivot +
            rotate(
                Offset(
                    PullCordDimens.ShadeRim.value / 2f - PullCordDimens.CordInset.value,
                    PullCordDimens.Rod.value + PullCordDimens.ShadeHeight.value,
                ),
                angle,
            )
}

private fun rotate(local: Offset, angle: Float): Offset {
    val c = cos(angle)
    val s = sin(angle)
    return Offset(local.x * c - local.y * s, local.x * s + local.y * c)
}
