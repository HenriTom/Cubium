package me.henritom.cubium.commands

import me.henritom.cubium.commands.impl.info.InfoCommand
import me.henritom.cubium.commands.impl.resetdefaultse.ResetDefaultSE
import me.henritom.cubium.commands.impl.feature.FeatureCommand
import me.henritom.cubium.commands.impl.overlay.setsize.OverlaySetsizeCommand
import me.henritom.cubium.commands.impl.warden.WardenCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

class CubiumCommand {
    companion object {
        fun register() {
            ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
                dispatcher.register(
                    ClientCommandManager.literal("cubium")
                        .then(InfoCommand().register())
                        .then(OverlaySetsizeCommand().register())
                        .then(ResetDefaultSE().register())
                        .then(FeatureCommand().register())
                        .then(WardenCommand().register())
                )
            }
        }
    }
}