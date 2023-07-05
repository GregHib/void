package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.client.variable.clear
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.map.area.Areas

val areas: Areas by inject()

val wilderness = areas.getValue("wilderness").area
val safeZones = areas.getTagged("safe_zone")

fun inWilderness(tile: Tile) = tile in wilderness && safeZones.none { tile in it.area }

on<Registered>({ inWilderness(it.tile) }, Priority.LOW) { player: Player ->
    player["in_wilderness"] = true
}

on<Moved>({ !inWilderness(from) && inWilderness(to) }) { player: Player ->
    player["in_wilderness"] = true
}

on<Moved>({ inWilderness(from) && !inWilderness(to) }) { player: Player ->
    player.clear("in_wilderness")
}