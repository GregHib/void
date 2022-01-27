package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.network.instruct.ChatTypeChange

class ChatTypeChangeHandler : InstructionHandler<ChatTypeChange>() {

    override fun validate(player: Player, instruction: ChatTypeChange) {
        player["chat_type"] = when (instruction.type) {
            1 -> "clan"
            else -> "public"
        }
    }

}