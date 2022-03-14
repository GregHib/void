package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.event.CancellableEvent

/**
 * FloorItem click before the attempt to walk within interact distance
 */
data class FloorItemClick(val floorItem: FloorItem, val option: String?) : CancellableEvent()