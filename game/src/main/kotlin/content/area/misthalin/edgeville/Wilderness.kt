package content.area.misthalin.edgeville

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile

@Script
class Wilderness : Api {

    val areas: AreaDefinitions by inject()

    val wilderness = areas["wilderness"]
    val safeZones = areas.getTagged("safe_zone")

    init {
        playerSpawn { player ->
            if (inWilderness(player.tile)) {
                player["in_wilderness"] = true
            }
        }
    }

    override fun move(player: Player, from: Tile, to: Tile) {
        val was = inWilderness(from)
        val now = inWilderness(to)
        if (!was && now) {
            player["in_wilderness"] = true
        } else if (was && !now) {
            player.clear("in_wilderness")
        }
    }

    fun inWilderness(tile: Tile) = tile in wilderness && safeZones.none { tile in it.area }
}
