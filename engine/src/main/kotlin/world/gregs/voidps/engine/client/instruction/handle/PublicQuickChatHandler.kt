package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.PublicQuickMessage
import world.gregs.voidps.network.instruct.PublicQuickChat

class PublicQuickChatHandler : InstructionHandler<PublicQuickChat>() {

    override fun validate(player: Player, instruction: PublicQuickChat) {
        player.events.emit(PublicQuickMessage(instruction.script, instruction.file, instruction.data))
    }

}