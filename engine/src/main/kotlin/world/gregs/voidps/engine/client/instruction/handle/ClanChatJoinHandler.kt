package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.JoinClanChat
import world.gregs.voidps.engine.entity.character.player.chat.clan.LeaveClanChat
import world.gregs.voidps.network.instruct.ClanChatJoin

class ClanChatJoinHandler : InstructionHandler<ClanChatJoin>() {

    override fun validate(player: Player, instruction: ClanChatJoin) {
        if (instruction.name.isBlank()) {
            player.events.emit(LeaveClanChat(forced = false))
        } else {
            player.events.emit(JoinClanChat(instruction.name))
        }
    }

}