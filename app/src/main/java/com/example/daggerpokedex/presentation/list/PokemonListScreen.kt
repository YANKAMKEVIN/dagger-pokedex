package com.example.daggerpokedex.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.IconButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.daggerpokedex.R
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.presentation.components.Pokeball
import com.example.daggerpokedex.presentation.components.PokedexErrorState
import com.example.daggerpokedex.presentation.components.StatusBarIcons
import com.example.daggerpokedex.presentation.components.pokemonSharedBounds
import com.example.daggerpokedex.presentation.components.pokemonSharedElement
import com.example.daggerpokedex.presentation.theme.PokeRed
import com.example.daggerpokedex.presentation.theme.cardColorForId

/**
 * Stateful entry point: subscribes to the ViewModel's [PokemonListState], owns the
 * (client-side) search query, and delegates rendering to stateless children.
 */
@Composable
fun PokemonListScreen(
    viewModel: PokemonListViewModel,
    gridState: LazyGridState,
    isMusicPlaying: Boolean,
    showMusicToggle: Boolean,
    onToggleMusic: () -> Unit,
    onPokemonClick: (Pokemon) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    // Search filters the already-loaded page(s) by name or Pokédex number (and falls back to a
    // lookup in the whole Pokédex, below, when nothing matches).
    val visiblePokemons = remember(state.pokemons, query) {
        val q = query.trim()
        if (q.isEmpty()) state.pokemons
        else state.pokemons.filter {
            it.name.contains(q, ignoreCase = true) || it.id.toString().contains(q)
        }
    }

    // Nothing loaded matches: after a short pause in typing, look the query up in the whole
    // Pokédex (by exact name or number) instead of answering "no results" straight away.
    val lookupKey = remember(query) { PokemonListViewModel.lookupKey(query) }
    val needsLookup = lookupKey != null && visiblePokemons.isEmpty() &&
        !state.isLoading && state.pokemons.isNotEmpty()
    LaunchedEffect(lookupKey, needsLookup) {
        if (needsLookup) {
            delay(400)
            viewModel.lookUp(query)
        }
    }
    val lookup = state.lookup?.takeIf { needsLookup && it.query == lookupKey }

    PokemonListContent(
        state = state,
        gridState = gridState,
        query = query,
        // Waiting for the debounce or the API: show a spinner rather than a premature "no results".
        isLookingUp = needsLookup && (lookup == null || lookup is SearchLookup.Loading),
        lookup = lookup,
        onQueryChange = { query = it },
        isMusicPlaying = isMusicPlaying,
        showMusicToggle = showMusicToggle,
        onToggleMusic = onToggleMusic,
        visiblePokemons = visiblePokemons,
        onPokemonClick = onPokemonClick,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadNextPage,
        onRetryLoadMore = viewModel::retryNextPage,
    )
}

@Composable
private fun PokemonListContent(
    state: PokemonListState,
    gridState: LazyGridState,
    query: String,
    isLookingUp: Boolean,
    lookup: SearchLookup?,
    onQueryChange: (String) -> Unit,
    isMusicPlaying: Boolean,
    showMusicToggle: Boolean,
    onToggleMusic: () -> Unit,
    visiblePokemons: List<Pokemon>,
    onPokemonClick: (Pokemon) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
) {
    // The light background needs dark status bar icons (and the dark theme light ones).
    StatusBarIcons(darkIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Keep the header below the status bar and clear of side bars / cutouts in landscape.
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        // Once the user scrolls past the first rows, fold the title away (only the search stays)
        // and offer a quick way back to the top.
        val collapsed by remember { derivedStateOf { gridState.firstVisibleItemIndex > 1 } }
        val showBackToTop by remember { derivedStateOf { gridState.firstVisibleItemIndex >= 6 } }
        val scope = rememberCoroutineScope()

        Header(
            query = query,
            onQueryChange = onQueryChange,
            collapsed = collapsed,
            isMusicPlaying = isMusicPlaying,
            showMusicToggle = showMusicToggle,
            onToggleMusic = onToggleMusic
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                // Ghost cards keep the splash -> list hand-off continuous while page 1 loads.
                state.isLoading -> SkeletonGrid()

                state.errorMessage != null && state.pokemons.isEmpty() ->
                    PokedexErrorState(message = state.errorMessage, onRetry = onRetry, accent = PokeRed)

                visiblePokemons.isEmpty() && lookup is SearchLookup.Found -> PokemonGrid(
                    pokemons = listOf(lookup.pokemon),
                    gridState = rememberLazyGridState(),
                    isLoadingMore = false,
                    loadMoreError = null,
                    paginationEnabled = false,
                    onPokemonClick = onPokemonClick,
                    onLoadMore = {},
                    onRetryLoadMore = {},
                )

                visiblePokemons.isEmpty() && isLookingUp -> CircularProgressIndicator(color = PokeRed)

                visiblePokemons.isEmpty() -> EmptySearchState(
                    query = query,
                    // Offline lookup: we only know the loaded Pokémon do not match.
                    loadedCount = if (lookup is SearchLookup.NotFound) null else state.pokemons.size,
                    canLoadMore = !state.endReached,
                    isLoadingMore = state.isLoadingMore,
                    onLoadMore = onLoadMore,
                    onClear = { onQueryChange("") }
                )

                else -> PokemonGrid(
                    pokemons = visiblePokemons,
                    gridState = gridState,
                    isLoadingMore = state.isLoadingMore,
                    loadMoreError = state.loadMoreError,
                    // Only page while browsing the full, unfiltered list.
                    paginationEnabled = query.isBlank(),
                    onPokemonClick = onPokemonClick,
                    onLoadMore = onLoadMore,
                    onRetryLoadMore = onRetryLoadMore,
                )
            }

            val bottomInset = WindowInsets.navigationBars.union(WindowInsets.ime)
                .asPaddingValues().calculateBottomPadding()
            BackToTopButton(
                visible = showBackToTop && !state.isLoading && visiblePokemons.isNotEmpty(),
                onClick = { scope.launch { gridState.animateScrollToItem(0) } },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp + bottomInset),
            )
        }
    }
}

/** Red Poké Ball button that scrolls the grid back to the first Pokémon. */
@Composable
private fun BackToTopButton(visible: Boolean, onClick: () -> Unit, modifier: Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier,
    ) {
        BackToTopFab(onClick)
    }
}

@Composable
private fun BackToTopFab(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = PokeRed,
        shadowElevation = 6.dp,
        modifier = Modifier.size(56.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Pokeball(color = Color.White.copy(alpha = 0.25f), filled = false, modifier = Modifier.fillMaxSize())
            Icon(
                painter = painterResource(R.drawable.ic_arrow_upward),
                contentDescription = "Back to top",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun MusicToggle(isMusicPlaying: Boolean, onToggleMusic: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onToggleMusic,
        shape = RoundedCornerShape(12.dp),
        color = if (isMusicPlaying) PokeRed.copy(alpha = 0.1f) else Color.Transparent,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(
                if (isMusicPlaying) R.drawable.ic_music_note else R.drawable.ic_music_off,
            ),
            contentDescription = if (isMusicPlaying) "Mute music" else "Play music",
            tint = if (isMusicPlaying) PokeRed else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(10.dp).size(24.dp),
        )
    }
}

/**
 * Bold title, a decorative Poké Ball peeking from the corner, and a search bar. When
 * [collapsed] (grid scrolled), the title folds away and the music toggle moves next to the
 * search bar, which stays pinned.
 */
@Composable
private fun Header(
    query: String,
    onQueryChange: (String) -> Unit,
    collapsed: Boolean,
    isMusicPlaying: Boolean,
    showMusicToggle: Boolean,
    onToggleMusic: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Faint ball peeking from the top-right. It must not give the header its height
        // (160 dp), or the folded header would keep an empty band above the grid.
        Box(modifier = Modifier.matchParentSize()) {
            Pokeball(
                color = PokeRed.copy(alpha = 0.10f),
                filled = false,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .wrapContentSize(Alignment.TopEnd, unbounded = true)
                    .offset(x = 40.dp, y = (-30).dp)
                    .size(160.dp),
            )
        }

        if (showMusicToggle) {
            AnimatedVisibility(
                visible = !collapsed,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
            ) {
                MusicToggle(isMusicPlaying = isMusicPlaying, onToggleMusic = onToggleMusic)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        ) {
            AnimatedVisibility(
                visible = !collapsed,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Pokédex",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Search a Pokémon by name or number",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    SearchField(query = query, onQueryChange = onQueryChange)
                }
                if (showMusicToggle) {
                    AnimatedVisibility(
                        visible = collapsed,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut(),
                    ) {
                        MusicToggle(
                            isMusicPlaying = isMusicPlaying,
                            onToggleMusic = onToggleMusic,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // Fixed height so the 48 dp clear button does not make the field grow when it appears.
            modifier = Modifier
                .heightIn(min = 52.dp)
                .padding(start = 18.dp, end = 4.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Box(modifier = Modifier
                .padding(start = 12.dp, end = if (query.isEmpty()) 14.dp else 0.dp)
                .weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "e.g. Pikachu or 025",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(PokeRed),
                    // Filtering is live: the keyboard action just closes the keyboard.
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PokemonGrid(
    pokemons: List<Pokemon>,
    gridState: LazyGridState,
    isLoadingMore: Boolean,
    loadMoreError: String?,
    paginationEnabled: Boolean,
    onPokemonClick: (Pokemon) -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
) {
    val shouldLoadMore by remember(pokemons.size, paginationEnabled) {
        derivedStateOf { paginationEnabled && gridState.reachedBottom(buffer = 6) }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    // The grid scrolls under the navigation bar (and the keyboard), but its last row can
    // still scroll fully above them.
    val bottomInset = WindowInsets.navigationBars.union(WindowInsets.ime)
        .asPaddingValues().calculateBottomPadding()

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + bottomInset),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(pokemons, key = { it.id }) { pokemon ->
            PokemonCard(pokemon = pokemon, onClick = { onPokemonClick(pokemon) })
        }

        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = PokeRed)
                }
            }
        } else if (loadMoreError != null && paginationEnabled) {
            // The auto-load only fires when the end of the grid comes into view: after a failure
            // the user is already there, so give them an explicit way to load the page again.
            item(span = { GridItemSpan(maxLineSpan) }) {
                LoadMoreError(message = loadMoreError, onRetry = onRetryLoadMore)
            }
        }
    }
}

@Composable
private fun LoadMoreError(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = "Couldn't load more Pokémon. $message",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun PokemonCard(pokemon: Pokemon, onClick: () -> Unit) {
    val base = cardColorForId(pokemon.id)
    // The card sinks slightly under the finger and springs back; opening it gives a short tick.
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardPress",
    )
    val haptics = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        color = base.copy(alpha = 0.16f),
        modifier = Modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .fillMaxWidth()
            // Morphs into the detail header during the shared-element transition.
            .pokemonSharedBounds("container-${pokemon.id}")
            .clip(RoundedCornerShape(20.dp)),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Watermark ball, bottom-right, half off-card.
            Pokeball(
                color = base.copy(alpha = 0.30f),
                filled = false,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 22.dp, y = 22.dp)
                    .size(96.dp),
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "#%03d".format(pokemon.id),
                    color = base,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
                Text(
                    text = pokemon.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(108.dp)
                        .align(Alignment.End),
                ) {
                    // A Poké Ball silhouette waits for the artwork, and stays if it fails to load.
                    var artworkShown by remember(pokemon.imageUrl) { mutableStateOf(false) }
                    if (!artworkShown) {
                        Pokeball(
                            color = base.copy(alpha = 0.35f),
                            filled = true,
                            modifier = Modifier.size(64.dp),
                        )
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(pokemon.imageUrl)
                            .crossfade(true) // fades in when downloaded, instant from the memory cache
                            .build(),
                        contentDescription = pokemon.name,
                        onState = { artworkShown = it is AsyncImagePainter.State.Success },
                        modifier = Modifier
                            .fillMaxSize()
                            // Flies into the detail header (same key on both screens).
                            .pokemonSharedElement("image-${pokemon.id}"),
                    )
                }
            }
        }
    }
}

/** Six ghost cards (same shape as [PokemonCard]) with a shimmer sweeping across them. */
@Composable
private fun SkeletonGrid() {
    val shimmer = rememberInfiniteTransition(label = "skeleton")
    val sweep by shimmer.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = LinearEasing)),
        label = "skeletonSweep",
    )
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Loading Pokémon" },
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + bottomInset),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(6) { index -> SkeletonCard(tint = cardColorForId(index + 1), sweep = sweep) }
    }
}

@Composable
private fun SkeletonCard(tint: Color, sweep: Float) {
    val bone = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val highlight = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(tint.copy(alpha = 0.10f))
            .drawWithContent {
                drawContent()
                // Diagonal light band moving left to right across the card.
                val x = size.width * sweep
                drawRect(
                    Brush.linearGradient(
                        colors = listOf(Color.Transparent, highlight, Color.Transparent),
                        start = Offset(x - size.width * 0.5f, 0f),
                        end = Offset(x + size.width * 0.5f, size.height),
                    ),
                )
            },
    ) {
        Pokeball(
            color = tint.copy(alpha = 0.18f),
            filled = false,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 22.dp, y = 22.dp)
                .size(96.dp),
        )
        Column(modifier = Modifier.padding(14.dp)) {
            Box(Modifier.padding(top = 2.dp).size(width = 40.dp, height = 12.dp).clip(RoundedCornerShape(50)).background(bone))
            Box(Modifier.padding(top = 8.dp).size(width = 96.dp, height = 16.dp).clip(RoundedCornerShape(50)).background(bone))
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .size(108.dp)
                    .align(Alignment.End),
                contentAlignment = Alignment.Center,
            ) {
                Pokeball(color = tint.copy(alpha = 0.22f), filled = true, modifier = Modifier.size(64.dp))
            }
        }
    }
}

/** True once the last item is within [buffer] positions of being visible. */
private fun LazyGridState.reachedBottom(buffer: Int): Boolean {
    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
    return lastVisible.index >= layoutInfo.totalItemsCount - 1 - buffer
}

/**
 * @param loadedCount null when the whole Pokédex was searched; otherwise only the [loadedCount]
 *   Pokémon loaded so far could be searched (offline), and more can be loaded if [canLoadMore].
 */
@Composable
private fun EmptySearchState(
    query: String,
    loadedCount: Int?,
    canLoadMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Une grosse Poké Ball très discrète en fond
            Pokeball(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                filled = false,
                modifier = Modifier.size(200.dp)
            )
            // Une icône qui évoque la recherche infructueuse
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp),
            )
        }

        Text(
            text = "No results found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = if (loadedCount == null) {
                "We couldn't find any Pokémon matching \"$query\". Check the spelling or try a number."
            } else {
                "None of the first $loadedCount Pokémon loaded match \"$query\", " +
                    "and the full Pokédex can't be searched right now."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (loadedCount != null && canLoadMore) {
                if (isLoadingMore) {
                    CircularProgressIndicator(color = PokeRed, modifier = Modifier.size(32.dp))
                } else {
                    OutlinedButton(onClick = onLoadMore, shape = RoundedCornerShape(50)) { Text("Load more") }
                }
            }
            Button(
                onClick = onClear,
                shape = RoundedCornerShape(50)
            ) {
                Text("Clear search")
            }
        }
    }
}
