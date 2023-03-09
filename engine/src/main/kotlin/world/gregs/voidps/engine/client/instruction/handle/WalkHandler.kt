package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.instruct.Walk

class WalkHandler : InstructionHandler<Walk>() {

    override fun validate(player: Player, instruction: Walk) {
        player.closeDialogue()
        player.closeMenu()
        player.queue.clearWeak()
        player.mode = EmptyMode
        player.suspension?.cancel()
        player.clearWatch()
        player.suspension = null
        player.interaction = null
        player.walkTo(player.tile.copy(instruction.x, instruction.y))
    }

}