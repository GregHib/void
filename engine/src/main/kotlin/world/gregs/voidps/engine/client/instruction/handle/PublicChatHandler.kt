package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.PublicMessage
import world.gregs.voidps.network.instruct.PublicChat

class PublicChatHandler : InstructionHandler<PublicChat>() {

    override fun validate(player: Player, instruction: PublicChat) {
        player.events.emit(PublicMessage(instruction.message, instruction.effects))
    }

}