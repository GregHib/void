package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.network.encode.packet21
import world.gregs.voidps.network.instruct.PublicQuickChat

class PublicQuickChatHandler : InstructionHandler<PublicQuickChat>() {

    override fun validate(player: Player, instruction: PublicQuickChat) {
        when (instruction.script) {
            0 -> {
                player.viewport.players.current.forEach {
                    it.client?.packet21(player.accountName, player.name, player.rights.ordinal, instruction.file, instruction.data)
                }
            }
        }

    }

}