package com.example.daggerpokedex.presentation.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.daggerpokedex.R
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.domain.model.PokemonDetail
import com.example.daggerpokedex.presentation.components.Pokeball
import com.example.daggerpokedex.presentation.components.PokedexErrorState
import com.example.daggerpokedex.presentation.components.pokemonSharedBounds
import com.example.daggerpokedex.presentation.components.pokemonSharedElement
import com.example.daggerpokedex.presentation.components.rememberCryPlayer
import com.example.daggerpokedex.presentation.components.StatusBarIcons
import com.example.daggerpokedex.presentation.theme.cardColorForId
import com.example.daggerpokedex.presentation.theme.colorForType
import com.example.daggerpokedex.presentation.theme.contentColorOn
import kotlin.math.abs

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
    onCryPlayingChange: (Boolean) -> Unit = {},
    neighbors: Neighbors = Neighbors(previous = null, next = null),
    onNavigate: (Pokemon) -> Unit = {},
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
    // White on most type colors, dark on the light ones (Pikachu's yellow, ice cyan...).
    val onAccent by animateColorAsState(contentColorOn(targetAccent), tween(450), label = "onAccent")

    val headerName = detail?.name ?: seed?.name ?: name
    val headerImage = detail?.imageUrl ?: seed?.imageUrl ?: ""
    val headerTypes = detail?.types ?: emptyList()

    // Stream and play the Pokémon's cry once the detail (hence its URL) loads,
    // then let the user replay it with the speaker button.
    val cryPlayer = rememberCryPlayer()
    val cryUrl = detail?.cryUrl
    LaunchedEffect(cryUrl) { cryUrl?.let(cryPlayer::play) }
    // Lets the background music step aside while the cry plays.
    val isCryPlaying = cryPlayer.isPlaying
    LaunchedEffect(isCryPlaying) { onCryPlayingChange(isCryPlaying) }
    DisposableEffect(Unit) { onDispose { onCryPlayingChange(false) } }

    // The colored header runs under the status bar: icons in the same color as its text.
    StatusBarIcons(darkIcons = contentColorOn(targetAccent) != Color.White)

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
            contentColor = onAccent,
            onBack = onBack,
            onCry = cryUrl?.let { url -> { cryPlayer.play(url) } },
            isCryPlaying = isCryPlaying,
            neighbors = neighbors,
            onNavigate = onNavigate,
        )

        when {
            detail != null -> StatsSection(detail = detail, accent = accent)

            state.errorMessage != null -> PokedexErrorState(
                message = state.errorMessage!!,
                onRetry = { viewModel.load(name) },
                accent = accent,
            )

            else -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = accent) }
        }

        // Lets the end of the page scroll above the navigation bar.
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun ColoredHeader(
    id: Int,
    name: String,
    imageUrl: String,
    types: List<String>,
    accent: Color,
    contentColor: Color,
    onBack: () -> Unit,
    onCry: (() -> Unit)? = null,
    isCryPlaying: Boolean = false,
    neighbors: Neighbors = Neighbors(previous = null, next = null),
    onNavigate: (Pokemon) -> Unit = {},
) {
    // The header is drawn under the status bar: grow it by the bar's height and push
    // its content (buttons, name) below the bar.
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp + statusBarTop)
            // The container that morphs from the tapped card.
            .pokemonSharedBounds("container-$id")
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(Brush.verticalGradient(listOf(accent, accent.copy(alpha = 0.75f))))
            // Swipe the header sideways to flip to the previous / next Pokémon.
            .pointerInput(neighbors, layoutDirection) {
                var drag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onHorizontalDrag = { _, amount -> drag += amount },
                    onDragEnd = {
                        val threshold = 72.dp.toPx()
                        val forward = if (layoutDirection == LayoutDirection.Rtl) drag > 0 else drag < 0
                        when {
                            abs(drag) < threshold -> Unit
                            forward -> neighbors.next?.let(onNavigate)
                            else -> neighbors.previous?.let(onNavigate)
                        }
                    },
                )
            },
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
            color = contentColor.copy(alpha = 0.25f),
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp + statusBarTop)
                .size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = contentColor,
                )
            }
        }

        // Replay the cry (top-right, mirroring the back button). It pulses while the cry plays.
        if (onCry != null) {
            val cryScale = remember { Animatable(1f) }
            LaunchedEffect(isCryPlaying) {
                if (isCryPlaying) {
                    while (true) {
                        cryScale.animateTo(1.15f, tween(300))
                        cryScale.animateTo(1f, tween(300))
                    }
                } else {
                    cryScale.animateTo(1f, tween(150))
                }
            }
            Surface(
                onClick = onCry,
                shape = RoundedCornerShape(50),
                color = contentColor.copy(alpha = 0.25f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp, top = 12.dp + statusBarTop)
                    .graphicsLayer {
                        scaleX = cryScale.value
                        scaleY = cryScale.value
                    }
                    .size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volume_up),
                        contentDescription = "Play cry",
                        tint = contentColor,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp + statusBarTop, start = 24.dp, end = 24.dp),
        ) {
            Text(
                text = "#%03d".format(id),
                color = contentColor.copy(alpha = 0.85f),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = name,
                color = contentColor,
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { type -> TypeChip(type, outline = contentColor) }
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

        // Drawn after the artwork so it never covers them on narrow screens.
        neighbors.previous?.let { previous ->
            NeighborButton(
                pokemon = previous,
                contentColor = contentColor,
                isNext = false,
                onClick = { onNavigate(previous) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 56.dp),
            )
        }
        neighbors.next?.let { next ->
            NeighborButton(
                pokemon = next,
                contentColor = contentColor,
                isNext = true,
                onClick = { onNavigate(next) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 56.dp),
            )
        }
    }
}

/** "‹ #024" / "#026 ›" pill that flips to the neighboring Pokémon. */
@Composable
private fun NeighborButton(
    pokemon: Pokemon,
    contentColor: Color,
    isNext: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = contentColor.copy(alpha = 0.25f),
        modifier = modifier.semantics {
            contentDescription = (if (isNext) "Next: " else "Previous: ") + pokemon.name
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 40.dp)
                .padding(start = if (isNext) 12.dp else 4.dp, end = if (isNext) 4.dp else 12.dp),
        ) {
            if (!isNext) {
                Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = null, tint = contentColor)
            }
            Text(
                text = "#%03d".format(pokemon.id),
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            if (isNext) {
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = contentColor)
            }
        }
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
            style = MaterialTheme.typography.titleLarge,
            color = accent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
        detail.stats.forEachIndexed { index, stat -> StatRow(stat = stat, index = index, accent = accent) }
        TotalRow(total = detail.stats.total(), accent = accent)
    }
}

/** A type in its own color, outlined so it stands out even on a header of the same color. */
@Composable
private fun TypeChip(type: String, outline: Color) {
    val color = colorForType(type)
    Surface(
        shape = RoundedCornerShape(50),
        color = color,
        border = BorderStroke(1.5.dp, outline.copy(alpha = 0.6f)),
    ) {
        Text(
            text = type,
            color = contentColorOn(color),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun InfoPill(label: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = accent)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Width of the label and value columns, shared by the stat rows and the total. */
private val StatLabelWidth = 64.dp
private val StatValueWidth = 40.dp

/**
 * One base stat: short label, value, and a bar that grows from 0 when the stats appear,
 * each row starting slightly after the previous one ([index]).
 */
@Composable
private fun StatRow(stat: PokemonDetail.Stat, index: Int, accent: Color) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val fraction by animateFloatAsState(
        targetValue = if (shown) (stat.value / 255f).coerceIn(0.02f, 1f) else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 80 * index, easing = FastOutSlowInEasing),
        label = "stat-${stat.name}",
    )
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            statLabel(stat.name),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(StatLabelWidth),
        )
        Text(
            stat.value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.width(StatValueWidth),
        )
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accent),
            )
        }
    }
}

@Composable
private fun TotalRow(total: Int, accent: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "TOTAL",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(StatLabelWidth),
        )
        Text(
            total.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = accent,
            textAlign = TextAlign.End,
            modifier = Modifier.width(StatValueWidth),
        )
    }
}
