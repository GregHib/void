package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.PrivateMessage
import world.gregs.voidps.network.instruct.PrivateChat

class PrivateChatHandler : InstructionHandler<PrivateChat>() {

    override fun validate(player: Player, instruction: PrivateChat) {
        player.events.emit(PrivateMessage(instruction.friend, instruction.message))
    }

}