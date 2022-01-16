package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.PrivateChat
import world.gregs.voidps.network.instruct.ChatPrivate

class ChatPrivateHandler : InstructionHandler<ChatPrivate>() {

    override fun validate(player: Player, instruction: ChatPrivate) {
        player.events.emit(PrivateChat(instruction.friend, instruction.message))
    }

}