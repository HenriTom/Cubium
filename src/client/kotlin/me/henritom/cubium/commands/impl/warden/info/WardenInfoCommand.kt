package me.henritom.cubium.commands.impl.warden.info

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.CubiumClient
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text

object WardenInfoCommand {

    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("info")
            .executes {
                MessageUtil.printTranslatable("cubium.warden.info", CubiumClient.warden.version, Text.translatable("cubium.warden.info.${if (CubiumClient.warden.enabled) "enabled" else "disabled"}").string)

                Command.SINGLE_SUCCESS
            }
    }
}