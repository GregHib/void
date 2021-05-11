package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.engine.event.Event

/**
 * FloorItem click before the attempt to walk within interact distance
 */
data class FloorItemClick(val floorItem: FloorItem, val option: String?) : Event {
    var cancel = false
}