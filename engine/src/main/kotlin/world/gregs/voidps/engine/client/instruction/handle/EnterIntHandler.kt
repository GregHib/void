package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.entity.character.player.Player

class EnterIntHandler : InstructionHandler<world.gregs.voidps.network.client.instruct.EnterInt>() {

    override fun validate(player: Player, instruction: world.gregs.voidps.network.client.instruct.EnterInt) {
        player.emit(IntEntered(instruction.value))
    }

}