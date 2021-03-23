package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile

object World : Entity {
    override val id: Int = 1
    override var tile = Tile.EMPTY
    override val events: Events = Events(this)
}