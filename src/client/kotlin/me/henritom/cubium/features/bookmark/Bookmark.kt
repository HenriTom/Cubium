package me.henritom.cubium.features.bookmark

import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(val id: Int, val name: String, val url: String, val folder: String)
