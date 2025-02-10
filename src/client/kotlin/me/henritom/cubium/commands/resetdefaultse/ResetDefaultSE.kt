package me.henritom.cubium.commands.resetdefaultse

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.CubiumClient
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text

class ResetDefaultSE {
    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("reset_default_se")
            .executes {
                if (CubiumClient.historyManager.lastUrl == Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine?.url).string)
                    CubiumClient.historyManager.lastUrl = ""

                CubiumClient.searchEngineManager.defaultSearchEngine = null

                Command.SINGLE_SUCCESS
            }
    }
}