package me.henritom.cubium.commands.impl.feature.list

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.CubiumClient
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object FeatureListCommand {

    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("list")
            .executes {
                MessageUtil.printTranslatable("cubium.features.list", CubiumClient.featureManager.features.size.toString())
                CubiumClient.featureManager.features.forEach { feature ->
                    MessageUtil.printTranslatable("cubium.features.list.it", if (feature.value) "§a${feature.key}" else "§c${feature.key}")
                }

                Command.SINGLE_SUCCESS
            }
    }
}