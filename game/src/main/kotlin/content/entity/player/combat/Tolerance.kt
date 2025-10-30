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
        playerSpawn {
            if (!contains("tolerance")) {
                start("tolerance", toleranceTime.toInt(), epochSeconds())
            }
            set("tolerance_area", tile.toCuboid(10))
        }
        moved {
            if (tile !in getOrPut("tolerance_area") { tile.toCuboid(10) }) {
                set("tolerance_area", tile.toCuboid(10))
                start("tolerance", toleranceTime.toInt(), epochSeconds())
            }
        }
    }
}
