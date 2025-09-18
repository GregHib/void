package content.area.misthalin.edgeville

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile

@Script
class Wilderness : Api {

    val areas: AreaDefinitions by inject()

    val wilderness = areas["wilderness"]
    val safeZones = areas.getTagged("safe_zone")

    override fun spawn(player: Player) {
        if (inWilderness(player.tile)) {
            player["in_wilderness"] = true
        }
    }

    init {
        move({ !inWilderness(from) && inWilderness(to) }) { player ->
            player["in_wilderness"] = true
        }

        move({ inWilderness(from) && !inWilderness(to) }) { player ->
            player.clear("in_wilderness")
        }
    }

    fun inWilderness(tile: Tile) = tile in wilderness && safeZones.none { tile in it.area }
}
