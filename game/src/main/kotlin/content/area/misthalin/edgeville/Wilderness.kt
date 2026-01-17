package content.area.misthalin.edgeville

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.type.Tile

class Wilderness : Script {

    val wilderness = Areas["wilderness"]
    val safeZones = Areas.tagged("safe_zone")

    init {
        playerSpawn {
            if (inWilderness(tile)) {
                set("in_wilderness", true)
            }
        }

        moved { from ->
            val was = inWilderness(from)
            val now = inWilderness(tile)
            if (!was && now) {
                set("in_wilderness", true)
            } else if (was && !now) {
                clear("in_wilderness")
            }
        }
    }

    fun inWilderness(tile: Tile) = tile in wilderness && safeZones.none { tile in it.area }
}
