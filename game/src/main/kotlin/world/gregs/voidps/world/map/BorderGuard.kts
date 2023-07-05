package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.zone.Zone
import world.gregs.voidps.type.Distance.nearestTo
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Cuboid
import world.gregs.voidps.type.area.Rectangle
import kotlin.collections.set

val objects: GameObjects by inject()
val areas: AreaDefinitions by inject()

val borders = mutableMapOf<Zone, Rectangle>()
val guards = mutableMapOf<Rectangle, List<GameObject>>()

on<World, Registered> {
    for (border in areas.getTagged("border")) {
        val passage = border.area as Cuboid
        for (zone in passage.toZones()) {
            val rectangle = passage.toRectangles().first()
            borders[zone] = rectangle
            guards[rectangle] = zone.toRectangle().mapNotNull {
                val obj = objects.getLayer(it, ObjectLayer.GROUND)
                if (obj != null && obj.id.startsWith("border_guard")) obj else null
            }
        }
    }
}

on<Moved>({ enteringBorder(from, to) }) { player: Player ->
    val border = borders[to.zone] ?: return@on
    player.start("no_clip", 3)
    player.start("slow_run", 3)
    if (player.steps.destination in border) {
        val tile = border.nearestTo(player.tile)
        val endSide = getOppositeSide(border, tile)
        player.walkTo(endSide)
    }
    val guards = guards[border] ?: return@on
    changeGuardState(guards, true)
}

on<Moved>({ exitingBorder(from, to) }) { player: Player ->
    val border = borders[to.zone] ?: return@on
    player.stop("no_clip")
    player.stop("slow_run")
    val guards = guards[border] ?: return@on
    changeGuardState(guards, false)
}

val raised = mutableMapOf<GameObject, Boolean>()

fun changeGuardState(guards: List<GameObject>, raise: Boolean) {
    for (guard in guards) {
        if (raised.getOrDefault(guard, false) != raise) {
            guard.animate(guard.def[if (raise) "raise" else "lower"])
            raised[guard] = raise
        }
    }
}

fun enteringBorder(from: Tile, to: Tile): Boolean {
    val border = borders[to.zone] ?: return false
    return !border.contains(from) && border.contains(to)
}

fun exitingBorder(from: Tile, to: Tile): Boolean {
    val border = borders[to.zone] ?: return false
    return border.contains(from) && !border.contains(to)
}

// Longest axis determines direction, current location above is underside else above
fun getOppositeSide(border: Rectangle, tile: Tile) = if (border.height > border.width) {
    tile.copy(y = if (tile.y > border.minY) border.minY - 1 else border.maxY + 1)
} else {
    tile.copy(x = if (tile.x > border.minX) border.minX - 1 else border.maxX + 1)
}