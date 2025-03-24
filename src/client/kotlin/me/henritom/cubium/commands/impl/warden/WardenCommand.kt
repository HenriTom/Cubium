package me.henritom.cubium.commands.impl.warden

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.commands.impl.warden.info.ToggleCommand
import me.henritom.cubium.commands.impl.warden.info.WardenInfoCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

class WardenCommand {
    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("warden")
            .then(ToggleCommand.register())
            .then(WardenInfoCommand.register())
    }
}