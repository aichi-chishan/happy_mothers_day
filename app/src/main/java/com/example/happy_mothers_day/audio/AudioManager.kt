package com.example.happy_mothers_day.audio

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.happy_mothers_day.R
import java.io.File

object AudioManager {

    private var mediaPlayer: MediaPlayer? = null
    private var currentContext: Context? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    var isPlaying: Boolean = false
        private set
    var duration: Int = 0
        private set
    var hasError: Boolean = false
        private set

    var currentSourcePath: String? = null
        private set
    var currentFileName: String = "默认音乐"
        private set
    var currentPositionMs: Int = 0
        private set

    var onStateChanged: (() -> Unit)? = null
    var mediaCallback: MediaCallback? = null

    interface MediaCallback {
        fun onPlay()
        fun onPause()
        fun onSeekTo(positionMs: Int)
    }

    /** Thread-safe notification */
    private fun notifyChanged() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onStateChanged?.invoke()
        } else {
            mainHandler.post { onStateChanged?.invoke() }
        }
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
                } else { hasError = true; null }
            } else {
                currentFileName = "默认音乐"
                currentSourcePath = null
                val m = MediaPlayer.create(context, R.raw.mothers_day_audio)
                if (m != null) {
                    m.setOnCompletionListener { this@AudioManager.isPlaying = false; notifyChanged() }
                    m
                } else { hasError = true; null }
            }
        } catch (_: Exception) { hasError = true; null }

        if (mp != null) {
            mediaPlayer = mp
            duration = mp.duration
            currentPositionMs = 0
            try {
                mp.start()
                isPlaying = true
                mediaCallback?.onPlay()
            } catch (e: Exception) {
                Log.e("AudioManager", "start failed", e)
                hasError = true
            }
        }
        notifyChanged()
    }

    fun togglePause() {
        val mp = mediaPlayer
        if (mp == null) {
            hasError = true
            notifyChanged()
            return
        }
        try {
            if (isPlaying) {
                mp.pause()
                isPlaying = false
                mediaCallback?.onPause()
            } else {
                mp.start()
                isPlaying = true
                mediaCallback?.onPlay()
            }
        } catch (_: Exception) {
            hasError = true
        }
        notifyChanged()
    }

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
