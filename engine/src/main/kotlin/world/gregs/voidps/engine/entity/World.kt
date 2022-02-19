package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.get

object World : Entity {
    override var tile = Tile.EMPTY
    override val size: Size = Size.ONE
    override val events: Events = Events(this)
    override val values: Values = Values()

    const val id = 16
    const val name = "World $id"

    fun start() {
        val store: EventHandlerStore = get()
        store.populate(World)
        events.emit(Registered)
    }

    fun shutdown() {
        events.emit(Unregistered)
        values.clear()
    }
}