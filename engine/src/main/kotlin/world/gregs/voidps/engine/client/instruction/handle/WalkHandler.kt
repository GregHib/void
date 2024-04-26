package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.Walk

class WalkHandler : InstructionHandler<Walk>() {

    override fun validate(player: Player, instruction: Walk) {
        if (player.hasClock("delay") || player.hasClock("input_delay")) {
            return
        }
        player.closeInterfaces()
        player.clearWatch()
        player.queue.clearWeak()
        player.suspension = null
        player.walkTo(player.tile.copy(instruction.x, instruction.y))
    }

}