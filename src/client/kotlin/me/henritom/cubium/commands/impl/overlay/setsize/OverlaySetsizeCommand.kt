package me.henritom.cubium.commands.impl.overlay.setsize

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.henritom.cubium.overlay.BrowserOverlay
import me.henritom.cubium.util.MessageUtil
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

class OverlaySetsizeCommand {
    fun register(): LiteralArgumentBuilder<FabricClientCommandSource>? {
        return ClientCommandManager.literal("overlay")
            .then(ClientCommandManager.literal("set_size")
                .then(ClientCommandManager.argument("x", DoubleArgumentType.doubleArg())
                    .then(ClientCommandManager.argument("y", DoubleArgumentType.doubleArg())
                        .executes {
                            val x = DoubleArgumentType.getDouble(it, "x")
                            val y = DoubleArgumentType.getDouble(it, "y")

                            BrowserOverlay.overlaySizeX = 10 / (x * 1.667)
                            BrowserOverlay.overlaySizeY = 10 / (y * 1.667)

                            MessageUtil.printTranslatable("cubium.overlay.set_size", x.toString(), y.toString())

                            Command.SINGLE_SUCCESS
                        })))
    }
}
