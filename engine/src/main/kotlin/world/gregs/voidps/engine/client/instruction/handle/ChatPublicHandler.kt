package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.global.PublicChat
import world.gregs.voidps.network.instruct.ChatPublic

class ChatPublicHandler : InstructionHandler<ChatPublic>() {

    override fun validate(player: Player, instruction: ChatPublic) {
        player.events.emit(PublicChat(instruction.message, instruction.effects))
    }

}