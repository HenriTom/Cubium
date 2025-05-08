package me.henritom.cubium.commands.impl.resetdefaultse

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.CubiumClient
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text

class ResetDefaultSE {
    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("reset_default_se")
            .executes {
                if (CubiumClient.historyManager.history.isNotEmpty() && CubiumClient.historyManager.history.last().url == Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine?.url).string)
                    CubiumClient.historyManager.history[CubiumClient.historyManager.history.size - 1].url = ""

                CubiumClient.searchEngineManager.defaultSearchEngine = null

                MessageUtil.printTranslatable("cubium.default_se.reset")

                Command.SINGLE_SUCCESS
            }
    }
}