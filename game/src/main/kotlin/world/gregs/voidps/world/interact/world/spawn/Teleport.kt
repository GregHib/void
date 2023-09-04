package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.type.Tile

data class Teleport(
    val id: String,
    val tile: Tile,
    val obj: ObjectDefinition,
    val option: String
) : CancellableEvent() {
    var delay: Int? = null
}