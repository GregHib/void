package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ignore.AddIgnore

class IgnoreAddHandler : InstructionHandler<world.gregs.voidps.network.client.instruct.IgnoreAdd>() {

    override fun validate(player: Player, instruction: world.gregs.voidps.network.client.instruct.IgnoreAdd) {
        player.emit(AddIgnore(instruction.name))
    }

}