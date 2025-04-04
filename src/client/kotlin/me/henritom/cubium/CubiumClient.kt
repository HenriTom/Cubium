package me.henritom.cubium

import me.henritom.cubium.commands.CubiumCommand
import me.henritom.cubium.config.ConfigManager
import me.henritom.cubium.features.FeatureManager
import me.henritom.cubium.features.warden.Warden
import me.henritom.cubium.features.bookmark.BookmarkManager
import me.henritom.cubium.features.history.HistoryManager
import me.henritom.cubium.features.uas.UserAgentManager
import me.henritom.cubium.keybinds.KeyBindManager
import me.henritom.cubium.overlay.BrowserOverlay
import me.henritom.cubium.search.SearchEngineManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback

object CubiumClient : ClientModInitializer {

	private var loaded = false

	val warden = Warden()
	var zoom = 0

	private val keyBindManager = KeyBindManager()
	val featureManager = FeatureManager()
	val configManager = ConfigManager()
	val searchEngineManager = SearchEngineManager()
	val userAgentManager = UserAgentManager()
	val historyManager = HistoryManager()
	val bookmarkManager = BookmarkManager()

	override fun onInitializeClient() {
		keyBindManager.registerKeyBinds()
		CubiumCommand.register()

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (!loaded) {
				if (client.isRunning) {
					warden.loadDefaults()

					configManager.checkForSearchEngines()

					configManager.loadSearchEngines()
					configManager.loadUserAgents()

					configManager.loadConfig()

					configManager.loadHistory()
					configManager.loadBookmarks()

					loaded = true

					HudRenderCallback.EVENT.register(BrowserOverlay())
				}
			}
		}

		Runtime.getRuntime().addShutdownHook(Thread {
			configManager.saveConfig()

			configManager.saveHistory()
			configManager.saveBookmarks()
		})
	}
}