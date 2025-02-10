package me.henritom.cubium.search

import me.henritom.cubium.CubiumClient

class SearchEngineManager {

    var defaultSearchEngine: SearchEngine? = null
    val searchEngines = mutableListOf<SearchEngine>()

    fun addSearchEngine(engine: SearchEngine): Boolean {
        return if (searchEngines.add(engine))
            CubiumClient.configManager.addSearchEngine(engine)
        else
            false
    }

    fun removeSearchEngine(engine: SearchEngine): Boolean {
        return if (searchEngines.remove(engine))
            CubiumClient.configManager.removeSearchEngine(engine)
        else
            false
    }

    fun getSearchEngineByTitle(title: String): SearchEngine? {
        return searchEngines.find { it.title == title }
    }
}