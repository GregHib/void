package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.EventCompanion
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get

/**
 * Replaces an existing map objects with [id] [tile] [type] and [rotation] provided.
 * The replacement can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to replaced just for one player.
 */
data class ReplaceObject(
    val gameObject: GameObject,
    val id: Int,
    val tile: Tile,
    val type: Int,
    val rotation: Int,
    val ticks: Int,
    val owner: String? = null
) : Event<Unit>() {
    companion object : EventCompanion<ReplaceObject>
}

fun GameObject.replace(id: Int, tile: Tile = this.tile, type: Int = this.type, rotation: Int = this.rotation, ticks: Int = -1, owner: String? = null) {
    get<EventBus>().emit(ReplaceObject(this, id, tile, type, rotation, ticks, owner))
}

fun replaceObject(
    original: GameObject,
    id: Int,
    tile: Tile,
    type: Int = 0,
    rotation: Int = 0,
    ticks: Int = -1,
    owner: String? = null
) = get<EventBus>().emit(ReplaceObject(original, id, tile, type, rotation, ticks, owner))