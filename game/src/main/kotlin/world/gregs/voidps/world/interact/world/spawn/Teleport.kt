package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile

data class Teleport(
    val obj: ObjectDefinition,
    val id: String,
    val option: String,
    val tile: Tile,
    val delta: Delta = Delta.EMPTY,
    val to: Tile = Tile.EMPTY
) : CancellableEvent() {
    var delay: Int? = null

    fun apply(tile: Tile): Tile {
        return if (delta != Delta.EMPTY) {
            tile.add(delta)
        } else if (to != Tile.EMPTY) {
            to
        } else {
            tile
        }
    }

    companion object {
        fun create(obj: ObjectDefinition, tele: Teleports.TeleportDefinition): Teleport {
            return Teleport(obj, tele.id, tele.option, tele.tile, tele.delta, tele.to)
        }
    }
}