package me.henritom.cubium.commands.impl.info.modrinth

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object ModrinthCommand {

    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("modrinth")
            .executes {
                MessageUtil.printTranslatableClickable("cubium.commands.info.modrinth", "https://modrinth.com/mod/cubium")

                Command.SINGLE_SUCCESS
            }
    }
}