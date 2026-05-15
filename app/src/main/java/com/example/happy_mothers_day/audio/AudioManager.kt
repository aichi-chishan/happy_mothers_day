package com.example.happy_mothers_day.audio

import android.content.Context
import android.media.MediaPlayer
import com.example.happy_mothers_day.R
import java.io.File

/**
 * Singleton that owns the single MediaPlayer instance.
 * All playback routes through here so only one audio stream exists.
 * Survives Activity recreation (rotation).
 */
object AudioManager {

    private var mediaPlayer: MediaPlayer? = null
    private var currentContext: Context? = null

    var isPlaying: Boolean = false
        private set
    var duration: Int = 0
        private set
    var hasError: Boolean = false
        private set

    /** Called by composable to register for state-change notifications */
    var onStateChanged: (() -> Unit)? = null

    private fun notifyChanged() {
        onStateChanged?.invoke()
    }

    fun play(context: Context, sourcePath: String?) {
        // Stop any existing playback first
        stopInternal()

        currentContext = context.applicationContext
        hasError = false

        val mp = try {
            if (!sourcePath.isNullOrEmpty()) {
                val file = File(sourcePath)
                if (file.exists()) {
                    val m = MediaPlayer()
                    m.setDataSource(sourcePath)
                    m.prepare()
                    m.setOnCompletionListener { this@AudioManager.isPlaying = false; notifyChanged() }
                    m
                } else {
                    hasError = true
                    null
                }
            } else {
                val m = MediaPlayer.create(context, R.raw.mothers_day_audio)
                if (m != null) {
                    m.setOnCompletionListener { this@AudioManager.isPlaying = false; notifyChanged() }
                    m
                } else {
                    hasError = true
                    null
                }
            }
        } catch (e: Exception) {
            hasError = true
            null
        }

        if (mp != null) {
            mediaPlayer = mp
            duration = mp.duration
            mp.start()
            isPlaying = true
        }
        notifyChanged()
    }

    fun togglePause() {
        val mp = mediaPlayer ?: return
        if (isPlaying) {
            mp.pause()
            isPlaying = false
        } else {
            mp.start()
            isPlaying = true
        }
        notifyChanged()
    }

    fun currentPosition(): Int {
        return try { mediaPlayer?.currentPosition ?: 0 } catch (_: Exception) { 0 }
    }

    fun seekTo(ms: Int) {
        try { mediaPlayer?.seekTo(ms) } catch (_: Exception) { }
        notifyChanged()
    }

    fun release() {
        stopInternal()
        currentContext = null
    }

    private fun stopInternal() {
        try { mediaPlayer?.stop() } catch (_: Exception) { }
        try { mediaPlayer?.release() } catch (_: Exception) { }
        mediaPlayer = null
        isPlaying = false
        duration = 0
        hasError = false
    }
}
