package me.henritom.cubium.util

import net.minecraft.client.MinecraftClient
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text

class MessageUtil {
    companion object {
        fun printTranslatable(key: String, vararg vars: String) {
            MinecraftClient.getInstance().inGameHud.chatHud.addMessage(Text.translatable(key, *vars.map { Text.literal(it) }.toTypedArray()))
        }

        fun printTranslatableClickable(message: String, urlString: String) {
            MinecraftClient.getInstance().inGameHud.chatHud.addMessage(Text.translatable(message).styled { style ->
                style.withClickEvent(
                    ClickEvent(ClickEvent.Action.OPEN_URL, urlString)
                )
            })
        }
    }
}