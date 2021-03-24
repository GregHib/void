package world.gregs.voidps.network.handle

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.path.Walk
import world.gregs.voidps.network.Handler

/**
 * @author GregHib <greg@gregs.world>
 * @since April 30, 2020
 */
class WalkMiniMapHandler : Handler() {

    override fun minimapWalk(player: Player, x: Int, y: Int, running: Boolean) {
        player.events.emit(Walk(player.tile.copy(x, y)))
    }

}