package me.henritom.cubium.commands.impl.feature

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.commands.impl.feature.list.FeatureListCommand
import me.henritom.cubium.commands.impl.feature.toggle.ToggleCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

class FeatureCommand {
    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("feature")
            .then(FeatureListCommand.register())
            .then(ToggleCommand.register())
    }
}