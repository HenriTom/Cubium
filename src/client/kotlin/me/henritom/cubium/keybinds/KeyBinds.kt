package me.henritom.cubium.keybinds

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class KeyBinds {

    fun registerKeyBinds() {
        val openBrowser = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "cubium.options.open_browser",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "cubium.options.category"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (openBrowser.wasPressed())
                client.setScreen(null)
        }
    }
}