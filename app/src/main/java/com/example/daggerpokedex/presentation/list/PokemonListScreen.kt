package com.example.daggerpokedex.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.daggerpokedex.domain.model.Pokemon
import com.example.daggerpokedex.presentation.components.Pokeball
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

    // Search filters the already-loaded page(s) by name or Pokédex number.
    val visiblePokemons = remember(state.pokemons, query) {
        val q = query.trim()
        if (q.isEmpty()) state.pokemons
        else state.pokemons.filter {
            it.name.contains(q, ignoreCase = true) || it.id.toString().contains(q)
        }
    }

    PokemonListContent(
        state = state,
        gridState = gridState,
        query = query,
        onQueryChange = { query = it },
        isMusicPlaying = isMusicPlaying,
        showMusicToggle = showMusicToggle,
        onToggleMusic = onToggleMusic,
        visiblePokemons = visiblePokemons,
        onPokemonClick = onPokemonClick,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadNextPage,
    )
}

@Composable
private fun PokemonListContent(
    state: PokemonListState,
    gridState: LazyGridState,
    query: String,
    onQueryChange: (String) -> Unit,
    isMusicPlaying: Boolean,
    showMusicToggle: Boolean,
    onToggleMusic: () -> Unit,
    visiblePokemons: List<Pokemon>,
    onPokemonClick: (Pokemon) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Header(
            query = query,
            onQueryChange = onQueryChange,
            isMusicPlaying = isMusicPlaying,
            showMusicToggle = showMusicToggle,
            onToggleMusic = onToggleMusic
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator(color = PokeRed)

                state.errorMessage != null && state.pokemons.isEmpty() ->
                    ErrorState(message = state.errorMessage, onRetry = onRetry)

                visiblePokemons.isEmpty() -> EmptySearchState(
                    query = query,
                    onClear = { onQueryChange("") }
                )

                else -> PokemonGrid(
                    pokemons = visiblePokemons,
                    gridState = gridState,
                    isLoadingMore = state.isLoadingMore,
                    // Only page while browsing the full, unfiltered list.
                    paginationEnabled = query.isBlank(),
                    onPokemonClick = onPokemonClick,
                    onLoadMore = onLoadMore,
                )
            }
        }
    }
}

/** Bold title, a decorative Poké Ball peeking from the corner, and a search bar. */
@Composable
private fun Header(
    query: String,
    onQueryChange: (String) -> Unit,
    isMusicPlaying: Boolean,
    showMusicToggle: Boolean,
    onToggleMusic: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Faint ball peeking from the top-right.
        Pokeball(
            color = PokeRed.copy(alpha = 0.10f),
            filled = false,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-30).dp)
                .size(160.dp),
        )

        if (showMusicToggle) {
            Surface(
                onClick = onToggleMusic,
                shape = RoundedCornerShape(12.dp),
                color = if (isMusicPlaying) PokeRed.copy(alpha = 0.1f) else Color.Transparent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = if (isMusicPlaying) "🔊" else "🔈",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 20.sp
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp),
        ) {
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
            SearchField(query = query, onQueryChange = onQueryChange)
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Text("🔍", fontSize = 16.sp)
            Box(modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxWidth()) {
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
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PokemonGrid(
    pokemons: List<Pokemon>,
    gridState: LazyGridState,
    isLoadingMore: Boolean,
    paginationEnabled: Boolean,
    onPokemonClick: (Pokemon) -> Unit,
    onLoadMore: () -> Unit,
) {
    val shouldLoadMore by remember(pokemons.size, paginationEnabled) {
        derivedStateOf { paginationEnabled && gridState.reachedBottom(buffer = 6) }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
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
        }
    }
}

@Composable
private fun PokemonCard(pokemon: Pokemon, onClick: () -> Unit) {
    val base = cardColorForId(pokemon.id)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = base.copy(alpha = 0.16f),
        modifier = Modifier
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
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(108.dp)
                        .align(Alignment.End)
                        // Flies into the detail header (same key on both screens).
                        .pokemonSharedElement("image-${pokemon.id}"),
                )
            }
        }
    }
}

/** True once the last item is within [buffer] positions of being visible. */
private fun LazyGridState.reachedBottom(buffer: Int): Boolean {
    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
    return lastVisible.index >= layoutInfo.totalItemsCount - 1 - buffer
}

@Composable
private fun EmptySearchState(query: String, onClear: () -> Unit) {
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
            // Un emoji ou une icône qui évoque la recherche infructueuse
            Text("🔍", fontSize = 48.sp, modifier = Modifier.offset(y = (-10).dp))
        }

        Text(
            text = "No results found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = "We couldn't find any Pokémon matching \"$query\". Check the spelling or try a number.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Button(
            onClick = onClear,
            shape = RoundedCornerShape(50)
        ) {
            Text("Clear search")
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(24.dp),
    ) {
        Text(text = message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetry) { Text("Retry") }
    }
}
