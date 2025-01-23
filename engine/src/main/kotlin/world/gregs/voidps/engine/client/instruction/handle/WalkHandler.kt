package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.Walk

class WalkHandler : InstructionHandler<Walk>() {

    override fun validate(player: Player, instruction: Walk) {
        if (player.contains("delay")) {
            return
        }
        player.closeInterfaces()
        player.clearWatch()
        player.queue.clearWeak()
        player.suspension = null
        if (instruction.minimap && !player["a_world_in_microcosm_task", false]) {
            player["a_world_in_microcosm_task"] = true
        }
        player.walkTo(player.tile.copy(instruction.x, instruction.y))
    }

}