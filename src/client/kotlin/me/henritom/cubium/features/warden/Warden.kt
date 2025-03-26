package me.henritom.cubium.features.warden

import java.net.HttpURLConnection
import java.net.URI

class Warden {

    val version = "1.0.0"

    val blockedDomains = mutableSetOf<String>()

    private fun addBlockList(url: String) {
        try {
            val connection = URI(url).toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.inputStream.bufferedReader().useLines { lines ->
                lines.filter { it.isNotBlank() && !it.startsWith("!") && !it.startsWith("[") }
                    .map { it.replace("^\\|\\|".toRegex(), "").replace("\\^.*".toRegex(), "") }
                    .forEach { blockedDomains.add(it) }
            }

            println("[Cubium Warden] Loaded blocklist with ${blockedDomains.size} entries")
        } catch (e: Exception) {
            println("[Cubium Warden] Failed to load blocklist")
        }
    }

    fun loadDefaults() {
        addBlockList("https://easylist.to/easylist/easylist.txt")
        addBlockList("https://easylist.to/easylist/easyprivacy.txt")
        addBlockList("https://secure.fanboy.co.nz/fanboy-cookiemonster.txt")
    }
}