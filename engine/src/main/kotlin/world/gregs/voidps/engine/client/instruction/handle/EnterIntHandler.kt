package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.emit
import world.gregs.voidps.network.instruct.EnterInt

class EnterIntHandler : InstructionHandler<EnterInt>() {

    override fun validate(player: Player, instruction: EnterInt) {
        player.emit(IntEntered(instruction.value))
    }

}