package com.example.happy_mothers_day.storage

import android.content.Context
import org.json.JSONObject

class TagAudioStorage(private val context: Context) {

    data class TagEntry(val tagId: String, val audioUri: String, val fileName: String)

    private val prefs
        get() = context.getSharedPreferences("tag_audio_mappings", Context.MODE_PRIVATE)

    fun saveMapping(tagId: String, audioUri: String, fileName: String) {
        val all = getAllMappings().toMutableList()
        all.removeAll { it.tagId == tagId }
        all.add(TagEntry(tagId, audioUri, fileName))
        writeAll(all)
    }

    fun removeMapping(tagId: String) {
        val normalized = tagId.lowercase()
        val all = getAllMappings().toMutableList()
        all.removeAll { it.tagId.lowercase() == normalized }
        writeAll(all)
    }

    fun getMapping(tagId: String): TagEntry? {
        val normalized = tagId.lowercase()
        return getAllMappings().find { it.tagId.lowercase() == normalized }
    }

    fun getAllMappings(): List<TagEntry> {
        val json = prefs.getString("mappings", "[]") ?: "[]"
        return try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                TagEntry(
                    tagId = obj.getString("tagId"),
                    audioUri = obj.getString("audioUri"),
                    fileName = obj.getString("fileName")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getDefaultTagId(): String? {
        return prefs.getString("default_tag_id", null)
    }

    fun setDefaultTagId(tagId: String?) {
        if (tagId != null) {
            prefs.edit().putString("default_tag_id", tagId).apply()
        } else {
            prefs.edit().remove("default_tag_id").apply()
        }
    }

    private fun writeAll(mappings: List<TagEntry>) {
        val arr = org.json.JSONArray()
        mappings.forEach {
            arr.put(JSONObject().apply {
                put("tagId", it.tagId)
                put("audioUri", it.audioUri)
                put("fileName", it.fileName)
            })
        }
        prefs.edit().putString("mappings", arr.toString()).apply()
    }
}
