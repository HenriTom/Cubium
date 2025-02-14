package me.henritom.cubium.features.history

class HistoryManager {

    var lastUrl = ""
    val history = mutableListOf<String>()

    fun add(url: String): Boolean {
        if (url.isEmpty() || url == history.lastOrNull() || url == lastUrl)
            return false

        lastUrl = history.lastOrNull() ?: ""
        history.add(url)

        return true
    }
}