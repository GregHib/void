package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ignore.DeleteIgnore
import world.gregs.voidps.network.instruct.IgnoreDelete

class IgnoreDeleteHandler : InstructionHandler<IgnoreDelete>() {

    override fun validate(player: Player, instruction: IgnoreDelete) {
        player.emit(DeleteIgnore(instruction.name))
    }

}