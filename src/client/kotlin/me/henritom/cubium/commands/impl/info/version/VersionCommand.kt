package me.henritom.cubium.commands.impl.info.version

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.loader.api.FabricLoader

object VersionCommand {

    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("version")
            .executes {
                MessageUtil.printTranslatable("cubium.commands.info.version", FabricLoader.getInstance().getModContainer("cubium").get().metadata.version.toString())

                Command.SINGLE_SUCCESS
            }
    }
}