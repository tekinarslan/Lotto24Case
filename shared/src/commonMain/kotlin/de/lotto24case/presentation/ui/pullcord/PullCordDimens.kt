package de.lotto24case.presentation.ui.pullcord

import androidx.compose.ui.unit.dp

/** The pull-cord lamp's sizes, timings and physics, in dp, seconds and milliseconds. */
internal object PullCordDimens {
    val Rod = 54.dp
    val CanopyWidth = 26.dp
    val CanopyHeight = 6.dp
    val ShadeTop = 18.dp
    val ShadeRim = 76.dp
    val ShadeHeight = 40.dp
    val BulbRadius = 11.dp

    val CordInset = 24.dp

    const val CordPoints = 12
    val CordLength = 77.dp
    val CordWidth = 1.6.dp
    val BeadRadius = 8.dp
    val GrabRadius = 40.dp

    val ClickPull = 48.dp
    val MostStretch = 30.dp

    const val StepSeconds = 1f / 120f
    const val ConstraintPasses = 24
    const val MostFrameSeconds = 0.05f

    const val Gravity = 3_200f
    const val CordDamping = 2.2f
    const val Recoil = 0.035f

    const val ShadeSwingHz = 1.3f
    const val ShadeDamping = 0.18f
    const val MostTilt = 0.35f
    const val TiltPerPull = 0.016f

    const val StillStep = 0.004f
    const val LevelTilt = 0.002f
    const val LevelSwing = 0.01f
    const val QuietSteps = 30

    const val RevealMs = 520
    const val WarmUpMs = 260
    const val OffMs = 120

    const val ConeHalfAngle = 0.78f
    const val ConeGlow = 0.65f
    const val HaloGlow = 0.60f
    val HaloRadius = 64.dp

    const val SpillGlow = 0.20f
    val SpillRadius = 240.dp
    val LightSource = 30.dp
    const val PoolGlow = 0.50f
    val PoolRadius = 140.dp

    const val ApexInside = 0.3f
    val RimCatch = 1.5.dp

    const val DraughtSpeed = 140f
    const val ShadeStir = 0.003f
}
