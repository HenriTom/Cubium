package me.henritom.cubium.features.history

class HistoryManager {

    var lastUrl = ""
    val history = mutableListOf<String>()

    fun add(url: String): Boolean {
        if (url.isEmpty())
            return false

        if (url == history.lastOrNull() || url == lastUrl || (history.size > 1 && url == history[history.size - 2]))
            return false

        lastUrl = history.lastOrNull() ?: ""
        history.add(url)

        return true
    }
}