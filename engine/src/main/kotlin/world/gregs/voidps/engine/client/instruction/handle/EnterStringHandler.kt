package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.event.StringEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.EnterString

class EnterStringHandler : InstructionHandler<EnterString>() {

    override fun validate(player: Player, instruction: EnterString) {
        player.emit(StringEntered(instruction.value))
    }

}