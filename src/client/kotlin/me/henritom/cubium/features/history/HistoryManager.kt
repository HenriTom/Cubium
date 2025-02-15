package me.henritom.cubium.features.history

class HistoryManager {

    var lastUrl = ""
    val history = mutableListOf<History>()

    fun add(url: String): Boolean {
        if (url.isEmpty() || url == history.lastOrNull()?.url || url == lastUrl)
            return false

        lastUrl = history.lastOrNull()?.url ?: ""
        history.add(History(System.currentTimeMillis(), url))

        return true
    }
}