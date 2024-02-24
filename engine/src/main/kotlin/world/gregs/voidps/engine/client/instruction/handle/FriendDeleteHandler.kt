package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.friend.DeleteFriend
import world.gregs.voidps.network.instruct.FriendDelete

class FriendDeleteHandler : InstructionHandler<FriendDelete>() {

    override fun validate(player: Player, instruction: FriendDelete) {
        player.emit(DeleteFriend(instruction.friendsName))
    }

}