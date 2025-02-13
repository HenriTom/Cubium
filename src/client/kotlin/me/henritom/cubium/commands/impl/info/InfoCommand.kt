package me.henritom.cubium.commands.impl.info

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.commands.impl.info.discord.DiscordCommand
import me.henritom.cubium.commands.impl.info.feedback.FeedbackCommand
import me.henritom.cubium.commands.impl.info.modrinth.ModrinthCommand
import me.henritom.cubium.commands.impl.info.wiki.WikiCommand
import me.henritom.cubium.commands.impl.info.version.VersionCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

class InfoCommand {
    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("info")
            .then(DiscordCommand.register())
            .then(FeedbackCommand.register())
            .then(ModrinthCommand.register())
            .then(VersionCommand.register())
            .then(WikiCommand.register())
    }
}
