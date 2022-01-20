package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.tick.Shutdown

object World : Entity {
    override var tile = Tile.EMPTY
    override val size: Size = Size.ONE
    override val events: Events = Events(this)
    override val values: Values = Values()

    const val id = 1
    const val name = "World $id"

    fun shutdown() {
        events.emit(Shutdown)
        events.clear()
        values.clear()
    }
}