package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.PublicQuickChat
import world.gregs.voidps.network.instruct.QuickChatPublic

class QuickChatPublicHandler : InstructionHandler<QuickChatPublic>() {

    override fun validate(player: Player, instruction: QuickChatPublic) {
        player.events.emit(PublicQuickChat(instruction.script, instruction.file, instruction.data))
    }

}