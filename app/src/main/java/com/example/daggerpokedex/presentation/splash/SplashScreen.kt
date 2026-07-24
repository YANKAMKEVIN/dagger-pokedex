package com.example.daggerpokedex.presentation.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daggerpokedex.presentation.components.Pokeball
import com.example.daggerpokedex.presentation.theme.PokeRed
import kotlinx.coroutines.delay

/**
 * Branded splash screen: a spinning Poké Ball that pops in over a red gradient,
 * then hands off to [onFinished] after a short beat. Purely presentational — it
 * owns no data and no Dagger dependency.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var started by remember { mutableStateOf(false) }

    // Entrance pop (scale + fade), driven once when the screen appears.
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.3f,
        animationSpec = tween(durationMillis = 700),
        label = "splashScale",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "splashAlpha",
    )

    // Continuous slow spin of the ball.
    val spin = rememberInfiniteTransition(label = "splashSpin")
    val rotation by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "splashRotation",
    )

    LaunchedEffect(Unit) {
        started = true
        delay(1700)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PokeRed, Color(0xFF9B0720)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.alpha(contentAlpha),
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                Pokeball(
                    color = Color.White,
                    filled = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation),
                )
            }
            Text(
                text = "Pokédex",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Powered by Dagger 2",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 0.dp),
            )
        }
    }
}
