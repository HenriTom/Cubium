package me.henritom.cubium.keybinds

import me.henritom.cubium.overlay.BrowserOverlay
import me.henritom.cubium.ui.impl.BrowserScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class KeyBindManager {

    fun registerKeyBinds() {
        val openBrowser = KeyBindingHelper.registerKeyBinding(KeyBinding("cubium.options.open_browser", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "cubium.options.category"))
        val scrollUp = KeyBindingHelper.registerKeyBinding(KeyBinding("cubium.options.scroll_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_UP, "cubium.options.category"))
        val scrollDown = KeyBindingHelper.registerKeyBinding(KeyBinding("cubium.options.scroll_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, "cubium.options.category"))

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (openBrowser.wasPressed())
                client.setScreen(BrowserScreen(null))

            if (scrollUp.wasPressed())
                BrowserOverlay.scroll(15)

            if (scrollDown.wasPressed())
                BrowserOverlay.scroll(-15)
        }
    }
}