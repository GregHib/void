package content.area.misthalin

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Distance.nearestTo
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.area.Rectangle
import kotlin.collections.set

val objects: GameObjects by inject()
val areas: AreaDefinitions by inject()

val borders = mutableMapOf<Zone, Rectangle>()
val guards = mutableMapOf<Rectangle, List<GameObject>>()

worldSpawn {
    for (border in areas.getTagged("border")) {
        val passage = border.area as Rectangle
        for (zone in passage.toZones()) {
            borders[zone] = passage
            guards[passage] = zone.toRectangle().mapNotNull {
                val obj = objects.getLayer(it, ObjectLayer.GROUND)
                if (obj != null && obj.id.startsWith("border_guard")) obj else null
            }
        }
    }
}

enterArea("border_guard*") {
    val border = area as Rectangle
    if (player.steps.destination in border) {
        val tile = border.nearestTo(player.tile)
        val endSide = getOppositeSide(border, tile)
        player.walkTo(endSide, noCollision = true, forceWalk = true)
    } else {
        player.steps.update(noCollision = true, noRun = true)
    }
    val guards = guards[border] ?: return@enterArea
    changeGuardState(guards, true)
}

exitArea("border_guard*") {
    val border = area as Rectangle
    val guards = guards[border] ?: return@exitArea
    player.steps.update(noCollision = false, noRun = false)
    changeGuardState(guards, false)
}

val raised = mutableMapOf<GameObject, Boolean>()

fun changeGuardState(guards: List<GameObject>, raise: Boolean) {
    for (guard in guards) {
        if (raised.getOrDefault(guard, false) != raise) {
            guard.anim(guard.def[if (raise) "raise" else "lower"])
            raised[guard] = raise
        }
    }
}

// Longest axis determines direction, current location above is underside else above
fun getOppositeSide(border: Rectangle, tile: Tile) = if (border.height > border.width) {
    tile.copy(y = if (tile.y > border.minY) border.minY - 1 else border.maxY + 1)
} else {
    tile.copy(x = if (tile.x > border.minX) border.minX - 1 else border.maxX + 1)
}