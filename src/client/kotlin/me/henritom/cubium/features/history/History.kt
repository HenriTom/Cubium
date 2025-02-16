package me.henritom.cubium.features.history

import kotlinx.serialization.Serializable

@Serializable
data class History(val time: Long, var url: String)
