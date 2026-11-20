package com.example.daggerpokedex.presentation.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Plays a Pokémon's cry by streaming the `.ogg` URL exposed by the PokéAPI.
 *
 * The audio is never bundled with the app — it is streamed on demand from the
 * public PokéAPI host, exactly like the official artwork the app already shows.
 * Nothing copyrighted is stored in the project.
 */
class CryPlayer {
    private var player: MediaPlayer? = null

    /** True while a cry is audible (observable from Compose). */
    var isPlaying by mutableStateOf(false)
        private set

    fun play(url: String) {
        release()
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            setOnPreparedListener { if (player === it) { it.start(); this@CryPlayer.isPlaying = true } }
            setOnCompletionListener { it.release(); if (player === it) { player = null; this@CryPlayer.isPlaying = false } }
            setOnErrorListener { mp, _, _ ->
                mp.release()
                if (player === mp) { player = null; this@CryPlayer.isPlaying = false }
                true
            }
            runCatching {
                setDataSource(url)
                prepareAsync()
            }.onFailure { release() }
        }
    }

    fun release() {
        player?.runCatching { release() }
        player = null
        isPlaying = false
    }
}

/** A [CryPlayer] that releases its MediaPlayer when it leaves composition. */
@Composable
fun rememberCryPlayer(): CryPlayer {
    val cryPlayer = remember { CryPlayer() }
    DisposableEffect(Unit) { onDispose { cryPlayer.release() } }
    return cryPlayer
}
