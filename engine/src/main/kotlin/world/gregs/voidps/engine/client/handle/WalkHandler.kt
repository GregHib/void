package world.gregs.voidps.engine.client.handle

import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Walk

class WalkHandler : Handler<Walk>() {

    override fun validate(player: Player, instruction: Walk) = sync {
        player.walkTo(player.tile.copy(instruction.x, instruction.y)) {
            if (player.movement.result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
        }
    }

}