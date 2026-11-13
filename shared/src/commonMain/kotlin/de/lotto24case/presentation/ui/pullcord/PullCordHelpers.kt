package de.lotto24case.presentation.ui.pullcord

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.hypot
import kotlin.math.max

data class PullCordColors(
    val lampShade: Color = Color(0xFF1E3A2F),
    val lampShadeSheen: Color = Color(0xFF4C7563),
    val lampBrass: Color = Color(0xFFC59F5A),
    val lampCord: Color = Color(0xFF8E8676),
    val bulbOff: Color = Color(0xFF8A847A),
    val bulbLit: Color = Color(0xFFFFF6D5),
    val lampLight: Color = Color(0xFFFFB84D),
)

internal fun farthestCorner(from: Offset, size: Size): Float =
    max(
        max(hypot(from.x, from.y), hypot(size.width - from.x, from.y)),
        max(hypot(from.x, size.height - from.y), hypot(size.width - from.x, size.height - from.y)),
    )

internal fun DrawScope.clipToReveal(
    circle: Path,
    from: Offset,
    progress: Float,
    inside: Boolean,
    content: DrawScope.() -> Unit,
) {
    val radius = farthestCorner(from, size) * progress
    circle.rewind()
    circle.addOval(Rect(from, radius))
    clipPath(circle, if (inside) ClipOp.Intersect else ClipOp.Difference) { content() }
}
