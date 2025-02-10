package me.henritom.cubium

import me.henritom.cubium.commands.CubiumCommand
import me.henritom.cubium.config.ConfigManager
import me.henritom.cubium.features.history.HistoryManager
import me.henritom.cubium.features.uas.UserAgentManager
import me.henritom.cubium.keybinds.KeyBindManager
import me.henritom.cubium.search.SearchEngineManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object CubiumClient : ClientModInitializer {

	private var loaded = false

	private val keyBindManager = KeyBindManager()
	val configManager = ConfigManager()
	val searchEngineManager = SearchEngineManager()
	val userAgentManager = UserAgentManager()
	val historyManager = HistoryManager()

	override fun onInitializeClient() {
		keyBindManager.registerKeyBinds()
		CubiumCommand.register()

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (!loaded) {
				if (client.isRunning) {
					configManager.checkForSearchEngines()

					configManager.loadSearchEngines()
					configManager.loadUserAgents()

					configManager.loadConfig()

					loaded = true
				}
			}
		}

		Runtime.getRuntime().addShutdownHook(Thread {
			configManager.saveConfig()
		})
	}
}