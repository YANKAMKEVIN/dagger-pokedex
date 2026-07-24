package com.example.daggerpokedex.presentation.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.model.PokemonDetail
import com.example.daggerpokedex.presentation.components.Pokeball
import com.example.daggerpokedex.presentation.components.pokemonSharedBounds
import com.example.daggerpokedex.presentation.components.pokemonSharedElement
import com.example.daggerpokedex.presentation.components.rememberCryPlayer
import com.example.daggerpokedex.presentation.theme.cardColorForId
import com.example.daggerpokedex.presentation.theme.colorForType

/**
 * @param seed the tapped list item, used to render the header (and drive the
 *   shared-element transition) instantly while the full detail loads. May be null
 *   after a configuration change, in which case we simply wait for the load.
 */
@Composable
fun PokemonDetailScreen(
    name: String,
    seed: Pokemon?,
    viewModel: PokemonDetailViewModel,
    onBack: () -> Unit,
) {
    LaunchedEffect(name) { viewModel.load(name) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val detail = state.detail

    // Prefer loaded data; fall back to the seed so the header exists from frame 1.
    val headerId = detail?.id ?: seed?.id
    if (headerId == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }
        return
    }

    // Option (a): start with the card's palette color, then smoothly fade to the
    // real primary-type color once the detail (hence the type) is known.
    val targetAccent =
        if (detail != null) colorForType(detail.types.firstOrNull() ?: "normal")
        else cardColorForId(headerId)
    val accent by animateColorAsState(targetAccent, tween(450), label = "accent")

    val headerName = detail?.name ?: seed?.name ?: name
    val headerImage = detail?.imageUrl ?: seed?.imageUrl ?: ""
    val headerTypes = detail?.types ?: emptyList()

    // Stream and play the Pokémon's cry once the detail (hence its URL) loads,
    // then let the user replay it with the speaker button.
    val cryPlayer = rememberCryPlayer()
    val cryUrl = detail?.cryUrl
    LaunchedEffect(cryUrl) { cryUrl?.let(cryPlayer::play) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ColoredHeader(
            id = headerId,
            name = headerName,
            imageUrl = headerImage,
            types = headerTypes,
            accent = accent,
            onBack = onBack,
            onCry = cryUrl?.let { url -> { cryPlayer.play(url) } },
        )

        when {
            detail != null -> StatsSection(detail = detail, accent = accent)

            state.errorMessage != null -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(state.errorMessage!!, textAlign = TextAlign.Center)
                Button(onClick = { viewModel.load(name) }) { Text("Retry") }
            }

            else -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = accent) }
        }
    }
}

@Composable
private fun ColoredHeader(
    id: Int,
    name: String,
    imageUrl: String,
    types: List<String>,
    accent: Color,
    onBack: () -> Unit,
    onCry: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            // The container that morphs from the tapped card.
            .pokemonSharedBounds("container-$id")
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(Brush.verticalGradient(listOf(accent, accent.copy(alpha = 0.75f)))),
    ) {
        Pokeball(
            color = Color.White.copy(alpha = 0.18f),
            filled = false,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 20.dp)
                .size(240.dp),
        )

        Surface(
            onClick = onBack,
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.25f),
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp)
                .size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("←", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Replay the cry (top-right, mirroring the back button).
        if (onCry != null) {
            Surface(
                onClick = onCry,
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp, top = 12.dp)
                    .size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🔊", fontSize = 20.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, start = 24.dp, end = 24.dp),
        ) {
            Text(
                text = "#%03d".format(id),
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = name,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { type -> TypeChip(type) }
            }
        }

        AsyncImage(
            model = imageUrl,
            contentDescription = name,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 24.dp)
                .size(210.dp)
                // The artwork that flies from the card.
                .pokemonSharedElement("image-$id"),
        )
    }
}

@Composable
private fun StatsSection(detail: PokemonDetail, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            InfoPill(label = "Height", value = "${detail.heightMeters} m", accent = accent)
            InfoPill(label = "Weight", value = "${detail.weightKilograms} kg", accent = accent)
        }

        Text(
            text = "Base stats",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
        detail.stats.forEach { stat -> StatRow(stat = stat, accent = accent) }
    }
}

@Composable
private fun TypeChip(type: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.25f),
    ) {
        Text(
            text = type,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun InfoPill(label: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accent)
        Text(
            label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatRow(stat: PokemonDetail.Stat, accent: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stat.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stat.value.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((stat.value / 255f).coerceIn(0.02f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accent),
            )
        }
    }
}
