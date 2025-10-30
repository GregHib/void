package content.area.misthalin.edgeville

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile

class Wilderness : Script {

    val areas: AreaDefinitions by inject()

    val wilderness = areas["wilderness"]
    val safeZones = areas.getTagged("safe_zone")

    init {
        playerSpawn { player ->
            if (inWilderness(player.tile)) {
                player["in_wilderness"] = true
            }
        }

        moved { player, from ->
            val was = inWilderness(from)
            val now = inWilderness(player.tile)
            if (!was && now) {
                player["in_wilderness"] = true
            } else if (was && !now) {
                player.clear("in_wilderness")
            }
        }
    }

    fun inWilderness(tile: Tile) = tile in wilderness && safeZones.none { tile in it.area }
}
