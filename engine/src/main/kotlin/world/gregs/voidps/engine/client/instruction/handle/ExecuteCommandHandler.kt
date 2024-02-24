package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.emit
import world.gregs.voidps.network.instruct.ExecuteCommand

class ExecuteCommandHandler : InstructionHandler<ExecuteCommand>() {

    override fun validate(player: Player, instruction: ExecuteCommand) {
        player.emit(Command(player, instruction.prefix, instruction.content))
    }

}