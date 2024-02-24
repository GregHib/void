package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.KickClanChat
import world.gregs.voidps.engine.event.emit
import world.gregs.voidps.network.instruct.ClanChatKick

class ClanChatKickHandler : InstructionHandler<ClanChatKick>() {

    override fun validate(player: Player, instruction: ClanChatKick) {
        player.emit(KickClanChat(instruction.name))
    }

}