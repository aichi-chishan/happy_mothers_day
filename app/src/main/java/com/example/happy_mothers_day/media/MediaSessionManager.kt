package com.example.happy_mothers_day.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import com.example.happy_mothers_day.MainActivity
import com.example.happy_mothers_day.R

class MediaSessionManager(private val context: Context) {

    private val mediaSession: MediaSession
    private val channelId = "playback_channel"
    private val notificationId = 1001
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var lastPositionMs: Long = 0
    private var lastDurationMs: Long = PlaybackState.PLAYBACK_POSITION_UNKNOWN

    private val callback = object : MediaSession.Callback() {
        override fun onPlay() { audioCallback?.onPlay() }
        override fun onPause() { audioCallback?.onPause() }
        override fun onSeekTo(pos: Long) { audioCallback?.onSeekTo(pos.toInt()) }
    }

    interface AudioCallback {
        fun onPlay()
        fun onPause()
        fun onSeekTo(positionMs: Int)
    }

    var audioCallback: AudioCallback? = null

    init {
        mediaSession = MediaSession(context, "HappyMothersDaySession").apply {
            setCallback(callback)
            @Suppress("DEPRECATION")
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
            isActive = true
        }
        createNotificationChannel()
    }

    /** Full state update: called on play/pause/seek with complete info */
    fun updatePlaybackState(isPlaying: Boolean, positionMs: Long, durationMs: Long) {
        lastPositionMs = positionMs
        lastDurationMs = durationMs

        val state = if (isPlaying) PlaybackState.STATE_PLAYING
        else if (durationMs > 0) PlaybackState.STATE_PAUSED
        else PlaybackState.STATE_NONE

        val builder = PlaybackState.Builder()
            .setState(state, positionMs, 1f)
            .setActions(
                PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_SEEK_TO or PlaybackState.ACTION_STOP
            )
        if (durationMs > 0) {
            builder.setBufferedPosition(durationMs)
        }
        mediaSession.setPlaybackState(builder.build())

        if (state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED) {
            showNotification(isPlaying)
        } else {
            hideNotification()
        }

        // Update metadata with duration so system shows progress bar
        if (durationMs > 0) {
            updateMetadata(null, null, durationMs)
        }
    }

    /** Lightweight position-only update — called periodically during playback */
    fun updatePosition(positionMs: Long, durationMs: Long) {
        lastPositionMs = positionMs
        lastDurationMs = durationMs
        val state = PlaybackState.Builder()
            .setState(PlaybackState.STATE_PLAYING, positionMs, 1f)
            .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_SEEK_TO or PlaybackState.ACTION_STOP)
        if (durationMs > 0) {
            state.setBufferedPosition(durationMs)
        }
        mediaSession.setPlaybackState(state.build())
    }

    fun updateMetadata(title: String?, artist: String?, durationMs: Long = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val builder = android.media.MediaMetadata.Builder()
            builder.putString(android.media.MediaMetadata.METADATA_KEY_TITLE, title ?: "母亲节快乐")
            builder.putString(android.media.MediaMetadata.METADATA_KEY_ARTIST, artist ?: "Happy Mother's Day")
            if (durationMs > 0) {
                builder.putLong(android.media.MediaMetadata.METADATA_KEY_DURATION, durationMs)
            }
            mediaSession.setMetadata(builder.build())
        }
    }

    fun hideNotification() {
        notificationManager.cancel(notificationId)
    }

    fun release() {
        hideNotification()
        mediaSession.isActive = false
        mediaSession.release()
    }

    private fun showNotification(isPlaying: Boolean) {
        val clickIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        @Suppress("DEPRECATION")
        val playAction = Notification.Action.Builder(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (isPlaying) "暂停" else "播放",
            null
        ).build()

        val notification = Notification.Builder(context, channelId)
            .setContentTitle("母亲节快乐")
            .setContentText("Happy Mother's Day")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(clickIntent)
            .setStyle(Notification.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .addAction(playAction)
            .setOngoing(isPlaying)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "音乐播放", NotificationManager.IMPORTANCE_LOW).apply {
                description = "音乐播放控制"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
