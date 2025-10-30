package content.entity.player.combat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.timer.epochSeconds
import java.util.concurrent.TimeUnit

/**
 * Certain NPCs stop being aggressive towards the player if they stay inside their tolerance area for [toleranceTime]
 */

class Tolerance : Script {

    val toleranceTime = TimeUnit.MINUTES.toSeconds(10)

    init {
        playerSpawn { player ->
            if (!player.contains("tolerance")) {
                player.start("tolerance", toleranceTime.toInt(), epochSeconds())
            }
            player["tolerance_area"] = player.tile.toCuboid(10)
        }
        moved { player, _ ->
            if (player.tile !in player.getOrPut("tolerance_area") { player.tile.toCuboid(10) }) {
                player["tolerance_area"] = player.tile.toCuboid(10)
                player.start("tolerance", toleranceTime.toInt(), epochSeconds())
            }
        }
    }
}
