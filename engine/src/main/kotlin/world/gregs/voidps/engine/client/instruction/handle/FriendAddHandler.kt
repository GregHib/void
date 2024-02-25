package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.friend.AddFriend
import world.gregs.voidps.network.instruct.FriendAdd

class FriendAddHandler : InstructionHandler<FriendAdd>() {

    override fun validate(player: Player, instruction: FriendAdd) {
        player.emit(AddFriend(instruction.friendsName))
    }

}