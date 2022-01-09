package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Tile

/**
 * Entity recently moved between [from] and [to] tiles
 * Emitted before [MovementTask] for a one tick delay for the client to display the movement
 */
data class Moved(val from: Tile, val to: Tile) : Event