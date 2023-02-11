package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Tile

/**
 * Entity moved between [from] and [to] tiles
 */
data class Moved(val from: Tile, val to: Tile) : Event