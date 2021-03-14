package world.gregs.voidps.handle

import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.encode.message

/**
 * @author GregHib <greg@gregs.world>
 * @since April 30, 2020
 */
class WalkMapHandler : Handler() {

    override fun walk(player: Player, x: Int, y: Int, running: Boolean) {
        player.walkTo(player.tile.copy(x = x, y = y)) { result ->
            if (result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
        }
    }

}