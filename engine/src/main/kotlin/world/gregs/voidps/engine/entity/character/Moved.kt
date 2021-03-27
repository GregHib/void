package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Tile

data class Moved(val from: Tile, val to: Tile) : Event