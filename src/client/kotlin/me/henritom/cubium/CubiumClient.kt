package me.henritom.cubium

import me.henritom.cubium.keybinds.KeyBinds
import net.fabricmc.api.ClientModInitializer

object CubiumClient : ClientModInitializer {
	override fun onInitializeClient() {
		KeyBinds().registerKeyBinds()
	}
}