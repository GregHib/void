package content.entity.player.combat

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

/**
 * Certain NPCs stop being aggressive towards the player if they stay inside their tolerance area for [toleranceTime]
 */
@Script
class Tolerance : Api {

    val toleranceTime = TimeUnit.MINUTES.toSeconds(10)

    override fun spawn(player: Player) {
        if (!player.contains("tolerance")) {
            player.start("tolerance", toleranceTime.toInt(), epochSeconds())
        }
        player["tolerance_area"] = player.tile.toCuboid(10)
    }

    override fun move(player: Player, from: Tile, to: Tile) {
        if (to !in player.getOrPut("tolerance_area") { player.tile.toCuboid(10) }) {
            player["tolerance_area"] = player.tile.toCuboid(10)
            player.start("tolerance", toleranceTime.toInt(), epochSeconds())
        }
    }
}
