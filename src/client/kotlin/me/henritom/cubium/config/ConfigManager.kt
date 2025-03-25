package me.henritom.cubium.config

import BookmarkListContainer
import HistoryListContainer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.serialization.json.Json
import me.henritom.cubium.CubiumClient
import me.henritom.cubium.features.uas.UserAgent
import me.henritom.cubium.search.SearchEngine
import me.henritom.cubium.ui.impl.BrowserScreen
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class ConfigManager {

    private var client = MinecraftClient.getInstance()
    private val configPath = FabricLoader.getInstance().configDir.resolve("cubium").toFile()

    fun deleteSearchEngines() {
        if (client == null)
            client = MinecraftClient.getInstance()

        val searchEnginesFile = File(configPath, "search_engines.json")

        if (searchEnginesFile.exists())
            searchEnginesFile.delete()
    }

    fun checkForSearchEngines() {
        if (client == null)
            client = MinecraftClient.getInstance()

        val searchEnginesFile = File(configPath, "search_engines.json")

        if (!searchEnginesFile.exists()) {
            val resourceManager = client.resourceManager
            val identifier = Identifier.of("cubium", "config/search_engines.json")

            try {
                val resource = resourceManager.getResource(identifier).get()
                val inputStream = resource.inputStream

                Files.createDirectories(searchEnginesFile.parentFile.toPath())

                Files.copy(inputStream, searchEnginesFile.toPath())
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadSearchEngines() {
        if (client == null)
            client = MinecraftClient.getInstance()

        try {
            val jsonInput = configPath.resolve("search_engines.json").reader(StandardCharsets.UTF_8).readText()
            val searchEngines = Json.decodeFromString<List<SearchEngine>>(jsonInput)

            for (searchEngine in searchEngines)
                CubiumClient.searchEngineManager.searchEngines.add(searchEngine)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addSearchEngine(engine: SearchEngine): Boolean {
        val json = Json { prettyPrint = true }
        val jsonInput = File(configPath, "search_engines.json").readText()
        val searchEngines = Json.decodeFromString<MutableList<SearchEngine>>(jsonInput)
        searchEngines.add(engine)
        File(configPath, "search_engines.json").writeText(json.encodeToString(searchEngines))
        return true
    }

    fun removeSearchEngine(engine: SearchEngine): Boolean {
        val json = Json { prettyPrint = true }
        val jsonInput = File(configPath, "search_engines.json").readText()
        val searchEngines = Json.decodeFromString<MutableList<SearchEngine>>(jsonInput)
        searchEngines.remove(engine)
        File(configPath, "search_engines.json").writeText(json.encodeToString(searchEngines))
        return true
    }

    fun loadUserAgents() {
        if (client == null)
            client = MinecraftClient.getInstance()

        val identifier = Identifier.of("cubium", "config/user_agents.json")

        try {
            val jsonInput = client.resourceManager.getResource(identifier).get().inputStream.reader(StandardCharsets.UTF_8).readText()
            val userAgents = Json.decodeFromString<List<UserAgent>>(jsonInput)

            for (userAgent in userAgents)
                CubiumClient.userAgentManager.userAgents.add(userAgent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveConfig() {
        val gson = GsonBuilder().setPrettyPrinting().create()

        val configFile = configPath.resolve("config.json")

        if (!configFile.parentFile.exists())
            configFile.parentFile.mkdirs()

        if (!configFile.exists())
            configFile.createNewFile()

        val data = mapOf(
            "default_se" to CubiumClient.searchEngineManager.defaultSearchEngine?.title,
            "user_agent" to CubiumClient.userAgentManager.userAgent,
            "warden" to CubiumClient.warden.enabled,
            "zoom" to CubiumClient.zoom
        )

        configFile.writeText(gson.toJson(data))
    }

    fun loadConfig() {
        val configFile = configPath.resolve("config.json")

        if (!configFile.exists() || configFile.isDirectory)
            return

        val data = Gson().fromJson(configFile.readText(), Map::class.java) as Map<*, *>
        CubiumClient.searchEngineManager.defaultSearchEngine = CubiumClient.searchEngineManager.getSearchEngineByTitle(data["default_se"] as? String ?: "")
        CubiumClient.userAgentManager.updateUserAgent(data["user_agent"] as? String ?: "")
        CubiumClient.warden.enabled = data["warden"] as? Boolean ?: true
        CubiumClient.zoom = (data["zoom"] as? Double ?: 0).toInt()
    }

    fun saveHistory() {
        val gson = GsonBuilder().setPrettyPrinting().create()

        val historyFile = configPath.resolve("history.json")

        if (!historyFile.parentFile.exists())
            historyFile.parentFile.mkdirs()

        if (!historyFile.exists())
            historyFile.createNewFile()

        val data = mapOf(
            "history" to CubiumClient.historyManager.history
        )

        historyFile.writeText(gson.toJson(data))
    }

    fun loadHistory() {
        val historyFile = configPath.resolve("history.json")

        if (!historyFile.exists() || historyFile.isDirectory)
            return

        try {
            val jsonInput = historyFile.readText(StandardCharsets.UTF_8)
            val container = Json.decodeFromString<HistoryListContainer>(jsonInput)

            CubiumClient.historyManager.history = container.history.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveBookmarks() {
        val gson = GsonBuilder().setPrettyPrinting().create()

        val bookmarksFile = configPath.resolve("bookmarks.json")

        if (!bookmarksFile.parentFile.exists())
            bookmarksFile.parentFile.mkdirs()

        if (!bookmarksFile.exists())
            bookmarksFile.createNewFile()

        val data = mapOf(
            "bookmarks" to CubiumClient.bookmarkManager.bookmarks
        )

        bookmarksFile.writeText(gson.toJson(data))
    }

    fun loadBookmarks() {
        val bookmarksFile = configPath.resolve("bookmarks.json")

        if (!bookmarksFile.exists() || bookmarksFile.isDirectory)
            return

        try {
            val jsonInput = bookmarksFile.readText(StandardCharsets.UTF_8)
            val container = Json.decodeFromString<BookmarkListContainer>(jsonInput)

            CubiumClient.bookmarkManager.bookmarks = container.bookmarks.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}