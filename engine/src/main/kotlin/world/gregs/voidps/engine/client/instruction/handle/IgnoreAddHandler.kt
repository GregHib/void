package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.AddIgnore
import world.gregs.voidps.network.instruct.IgnoreAdd

class IgnoreAddHandler : InstructionHandler<IgnoreAdd>() {

    override fun validate(player: Player, instruction: IgnoreAdd) {
        player.events.emit(AddIgnore(instruction.name))
    }

}