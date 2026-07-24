package com.example.daggerpokedex.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * A Poké Ball drawn entirely with Canvas primitives (no image asset needed).
 * Used both as the animated splash logo and as a faint watermark behind cards.
 *
 * @param color the ball's line/silhouette color. Alpha is respected, so passing a
 *   translucent color turns it into a subtle watermark.
 * @param filled when true, fills the top half and center; when false, only the
 *   outline is drawn (used for the monochrome watermark).
 */
@Composable
fun Pokeball(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    filled: Boolean = false,
) {
    Canvas(modifier = modifier) {
        drawPokeball(color = color, filled = filled)
    }
}

private fun DrawScope.drawPokeball(color: Color, filled: Boolean) {
    val d = size.minDimension
    val stroke = d * 0.06f
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = d / 2f - stroke

    if (filled) {
        // Top half.
        drawArc(
            color = color.copy(alpha = color.alpha * 0.9f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        )
    }

    // Outer ring.
    drawCircle(color = color, radius = radius, center = center, style = Stroke(width = stroke))
    // Middle band.
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = stroke,
    )
    // Center button.
    val buttonR = d * 0.16f
    drawCircle(color = color, radius = buttonR, center = center, style = Stroke(width = stroke))
    if (filled) {
        drawCircle(color = Color.White, radius = buttonR - stroke / 2f, center = center)
        drawCircle(color = color, radius = buttonR * 0.45f, center = center)
    }
}
