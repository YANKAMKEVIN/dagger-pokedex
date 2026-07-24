package com.example.daggerpokedex.presentation.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * Plays a Pokémon's cry by streaming the `.ogg` URL exposed by the PokéAPI.
 *
 * The audio is never bundled with the app — it is streamed on demand from the
 * public PokéAPI host, exactly like the official artwork the app already shows.
 * Nothing copyrighted is stored in the project.
 */
class CryPlayer {
    private var player: MediaPlayer? = null

    fun play(url: String) {
        release()
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            setOnPreparedListener { start() }
            setOnCompletionListener { it.release(); if (player === it) player = null }
            setOnErrorListener { mp, _, _ -> mp.release(); if (player === mp) player = null; true }
            runCatching {
                setDataSource(url)
                prepareAsync()
            }.onFailure { release() }
        }
    }

    fun release() {
        player?.runCatching { release() }
        player = null
    }
}

/** A [CryPlayer] that releases its MediaPlayer when it leaves composition. */
@Composable
fun rememberCryPlayer(): CryPlayer {
    val cryPlayer = remember { CryPlayer() }
    DisposableEffect(Unit) { onDispose { cryPlayer.release() } }
    return cryPlayer
}
