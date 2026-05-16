package com.example.happy_mothers_day.util

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever

object AlbumArtExtractor {
    fun extract(filePath: String): android.graphics.Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val art = retriever.embeddedPicture
            if (art != null) BitmapFactory.decodeByteArray(art, 0, art.size) else null
        } catch (_: Exception) {
            null
        } finally {
            try { retriever.release() } catch (_: Exception) { }
        }
    }
}
