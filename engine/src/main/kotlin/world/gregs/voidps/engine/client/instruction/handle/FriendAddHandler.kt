package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.FriendAdd

class FriendAddHandler : InstructionHandler<FriendAdd>() {

    private val players: Players by inject()

    override fun validate(player: Player, instruction: FriendAdd) {
        val friend = players.get(instruction.friendsName)
        if (friend == null) {
            player.message("Could not find player with the username '${instruction.friendsName}'.")
            return
        }
//        player.events.emit(instruction)
    }

}