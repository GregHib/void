package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.PrivateQuickMessage
import world.gregs.voidps.network.instruct.PrivateQuickChat

class PrivateQuickChatHandler : InstructionHandler<PrivateQuickChat>() {

    override fun validate(player: Player, instruction: PrivateQuickChat) {
        player.events.emit(PrivateQuickMessage(instruction.name, instruction.file, instruction.data))
    }

}