package com.example.happy_mothers_day.audio

import android.content.Context
import android.media.MediaPlayer
import com.example.happy_mothers_day.R
import java.io.File

object AudioManager {

    private var mediaPlayer: MediaPlayer? = null
    private var currentContext: Context? = null

    var isPlaying: Boolean = false
        private set
    var duration: Int = 0
        private set
    var hasError: Boolean = false
        private set

    /** null = built-in default, non-null = custom file path */
    var currentSourcePath: String? = null
        private set
    var currentFileName: String = "默认音乐"
        private set
    var currentPositionMs: Int = 0
        private set

    /** Derived: progress as fraction 0..1 */
    val seekPosition: Float
        get() = if (duration > 0) currentPositionMs.toFloat() / duration else 0f

    var onStateChanged: (() -> Unit)? = null

    private fun notifyChanged() {
        onStateChanged?.invoke()
    }

    fun play(context: Context, sourcePath: String?) {
        stopInternal()

        currentContext = context.applicationContext
        currentSourcePath = sourcePath
        hasError = false

        val mp = try {
            if (!sourcePath.isNullOrEmpty()) {
                val file = File(sourcePath)
                if (file.exists()) {
                    currentFileName = file.name
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
                currentFileName = "默认音乐"
                currentSourcePath = null
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
            currentPositionMs = 0
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

    /** Call from composable polling loop to sync position */
    fun pollPosition() {
        currentPositionMs = try { mediaPlayer?.currentPosition ?: 0 } catch (_: Exception) { 0 }
    }

    fun seekToFraction(fraction: Float) {
        val ms = (fraction * duration).toInt().coerceIn(0, duration)
        currentPositionMs = ms
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
        currentPositionMs = 0
        hasError = false
        currentSourcePath = null
        currentFileName = "默认音乐"
    }
}
