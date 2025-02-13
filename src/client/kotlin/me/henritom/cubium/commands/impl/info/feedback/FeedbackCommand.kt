package me.henritom.cubium.commands.impl.info.feedback

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object FeedbackCommand {

    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("feedback")
            .executes {
                MessageUtil.printTranslatableClickable("cubium.commands.info.feedback", "https://github.com/HenriTom/Cubium/issues/new/choose")

                Command.SINGLE_SUCCESS
            }
    }
}