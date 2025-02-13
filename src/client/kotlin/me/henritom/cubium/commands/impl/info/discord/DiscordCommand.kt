package me.henritom.cubium.commands.impl.info.discord

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object DiscordCommand {

    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("discord")
            .executes {
                MessageUtil.printTranslatableClickable("cubium.commands.info.discord", "https://discord.gg/XdHBJKTvxJ")

                Command.SINGLE_SUCCESS
            }
    }
}