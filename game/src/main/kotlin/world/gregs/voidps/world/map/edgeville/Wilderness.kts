package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile

val areas: AreaDefinitions by inject()

val wilderness = areas["wilderness"]
val safeZones = areas.getTagged("safe_zone")

fun inWilderness(tile: Tile) = tile in wilderness && safeZones.none { tile in it.area }

playerSpawn({ inWilderness(it.tile) }, Priority.LOW) { player: Player ->
    player["in_wilderness"] = true
}

move({ !inWilderness(from) && inWilderness(to) }) { player: Player ->
    player["in_wilderness"] = true
}

move({ inWilderness(from) && !inWilderness(to) }) { player: Player ->
    player.clear("in_wilderness")
}