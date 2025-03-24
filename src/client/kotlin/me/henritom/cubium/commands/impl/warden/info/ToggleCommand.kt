package me.henritom.cubium.commands.impl.warden.info

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.CubiumClient
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text

object ToggleCommand {

    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("toggle")
            .executes {
                MessageUtil.printTranslatable("cubium.warden.toggled", Text.translatable("cubium.warden.info.${if (!CubiumClient.warden.enabled) "enabled" else "disabled"}").string)
                CubiumClient.warden.enabled = !CubiumClient.warden.enabled

                Command.SINGLE_SUCCESS
            }
    }
}