package world.gregs.voidps.engine.path

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Tile

data class Walk(val to: Tile) : Event