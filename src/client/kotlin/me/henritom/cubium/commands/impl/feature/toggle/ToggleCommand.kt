package me.henritom.cubium.commands.impl.feature.toggle

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.CubiumClient
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text

object ToggleCommand {

    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("toggle")
            .then(ClientCommandManager.argument("feature", StringArgumentType.string())
                .suggests { _, builder ->
                    CubiumClient.featureManager.features.keys.forEach { builder.suggest(it) }
                    builder.buildFuture()
                }
                .executes {
                    val feature = StringArgumentType.getString(it, "feature")

                    if (CubiumClient.featureManager.features.containsKey(feature)) {
                        CubiumClient.featureManager.features[feature] = !CubiumClient.featureManager.features[feature]!!
                        MessageUtil.printTranslatable("cubium.features.toggled", feature, Text.translatable(if (CubiumClient.featureManager.features[feature] == true) "cubium.warden.info.enabled" else "cubium.warden.info.disabled").string)
                        Command.SINGLE_SUCCESS
                    } else
                        MessageUtil.printTranslatable("cubium.features.not_found", feature)

                    Command.SINGLE_SUCCESS
                })
    }
}