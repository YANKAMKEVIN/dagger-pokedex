package com.example.daggerpokedex.presentation.components

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Looping background music. It plays by default; the user's on/off choice is remembered
 * across launches, and the music is turned down while a Pokémon cry plays.
 *
 * ## No copyrighted asset ships with the app
 * The track is resolved **dynamically** by name (`res/raw/bgm.*`) at runtime, so
 * the project builds and runs fine with no music file at all — the toggle simply
 * does not appear. Drop a royalty-free / CC0 track at `res/raw/bgm.mp3` (or
 * `.ogg`) and the control lights up. Nothing musical is committed to the repo.
 */
class MusicController(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var player: MediaPlayer? = null
    private var wantPlaying = false
    private var ducked = false

    /** The last on/off choice, kept across launches (on until the user turns it off). */
    val savedPreference: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_ON, true)

    /** True only when a `res/raw/bgm.*` track is present. */
    val hasTrack: Boolean
        get() = trackResId() != 0

    private fun trackResId(): Int =
        appContext.resources.getIdentifier("bgm", "raw", appContext.packageName)

    private fun ensurePlayer(): MediaPlayer? {
        if (player == null) {
            val resId = trackResId()
            if (resId == 0) return null
            player = MediaPlayer.create(appContext, resId)?.apply {
                isLooping = true
                applyVolume(this)
            }
        }
        return player
    }

    // Stay in the background; while a cry plays, almost fade out so it is clearly heard.
    private fun applyVolume(p: MediaPlayer) {
        val volume = if (ducked) DUCKED_VOLUME else NORMAL_VOLUME
        p.setVolume(volume, volume)
    }

    /** Turns the music down while a Pokémon cry plays, and back up afterwards. */
    fun duck(duck: Boolean) {
        if (ducked == duck) return
        ducked = duck
        player?.let(::applyVolume)
    }

    /** User intent: turn the music on or off (remembered for the next launch). */
    fun setPlaying(play: Boolean) {
        wantPlaying = play
        prefs.edit().putBoolean(KEY_MUSIC_ON, play).apply()
        val p = ensurePlayer() ?: return
        if (play && !p.isPlaying) p.start() else if (!play && p.isPlaying) p.pause()
    }

    /** Lifecycle: pause when the app leaves the foreground, resume if desired. */
    fun onAppStopped() {
        player?.takeIf { it.isPlaying }?.pause()
    }

    fun onAppStarted() {
        if (wantPlaying) ensurePlayer()?.takeIf { !it.isPlaying }?.start()
    }

    fun release() {
        player?.runCatching { release() }
        player = null
    }

    private companion object {
        const val PREFS_NAME = "audio"
        const val KEY_MUSIC_ON = "music_on"
        const val NORMAL_VOLUME = 0.35f
        const val DUCKED_VOLUME = 0.08f
    }
}

/** Creates a [MusicController] tied to the current lifecycle (pauses in background). */
@Composable
fun rememberMusicController(): MusicController {
    val context = LocalContext.current
    val controller = remember { MusicController(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> controller.onAppStopped()
                Lifecycle.Event.ON_START -> controller.onAppStarted()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.release()
        }
    }
    return controller
}
