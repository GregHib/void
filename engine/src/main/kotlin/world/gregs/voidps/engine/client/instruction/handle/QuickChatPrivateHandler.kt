package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.friend.PrivateQuickChat
import world.gregs.voidps.network.instruct.QuickChatPrivate

class QuickChatPrivateHandler : InstructionHandler<QuickChatPrivate>() {

    override fun validate(player: Player, instruction: QuickChatPrivate) {
        player.emit(PrivateQuickChat(instruction.name, instruction.file, instruction.data))
    }

}