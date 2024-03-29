package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.UpdateClanChatRank
import world.gregs.voidps.network.client.instruction.ClanChatRank

class ClanChatRankHandler : InstructionHandler<ClanChatRank>() {

    override fun validate(player: Player, instruction: ClanChatRank) {
        player.emit(UpdateClanChatRank(instruction.name, instruction.rank))
    }

}