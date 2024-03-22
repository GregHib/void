package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.global.PublicQuickChat

class QuickChatPublicHandler : InstructionHandler<world.gregs.voidps.network.client.instruction.QuickChatPublic>() {

    override fun validate(player: Player, instruction: world.gregs.voidps.network.client.instruction.QuickChatPublic) {
        player.emit(PublicQuickChat(instruction.script, instruction.file, instruction.data))
    }

}