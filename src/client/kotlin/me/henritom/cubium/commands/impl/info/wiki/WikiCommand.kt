package me.henritom.cubium.commands.impl.info.wiki

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object WikiCommand {

    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("wiki")
            .executes {
                MessageUtil.printTranslatableClickable("cubium.commands.info.wiki", "https://github.com/HenriTom/cubium/wiki")

                Command.SINGLE_SUCCESS
            }
    }
}