package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Tile

/**
 * Recently moved between [from] and [to] tiles and needs map update
 * Emits after [MovementTask] instead of before like [Moved]
 * @see Moved for checking entity movement for content
 */
data class Moving(val from: Tile, val to: Tile) : Event