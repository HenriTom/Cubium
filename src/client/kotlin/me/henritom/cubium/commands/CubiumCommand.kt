package me.henritom.cubium.commands

import me.henritom.cubium.commands.impl.info.InfoCommand
import me.henritom.cubium.commands.impl.resetdefaultse.ResetDefaultSE
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

class CubiumCommand {
    companion object {
        fun register() {
            ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
                dispatcher.register(
                    ClientCommandManager.literal("cubium")
                        .then(InfoCommand().register())
                        .then(ResetDefaultSE().register())
                )
            }
        }
    }
}