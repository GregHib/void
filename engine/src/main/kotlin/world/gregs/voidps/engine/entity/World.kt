package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timer.Job
import world.gregs.voidps.engine.timer.QueuedTimers
import world.gregs.voidps.engine.utility.get

const val MAX_PLAYERS = 0x800 // 2048
const val MAX_NPCS = 0x8000 // 32768

object World : Entity {
    override var tile = Tile.EMPTY
    override val size: Size = Size.ONE
    override val events: Events = Events(this)
    override var values: Values? = Values()

    val timers = QueuedTimers()

    fun timer(ticks: Int = 0, loop: Boolean = false, cancelExecution: Boolean = false, block: Job.(Long) -> Unit) = timers.add(ticks, if (loop) ticks else -1, cancelExecution, block)

    const val id = 16
    const val name = "World $id"

    fun start(members: Boolean) {
        values?.set("members", members)
        val store: EventHandlerStore = get()
        store.populate(World)
        events.emit(Registered)
    }

    fun shutdown() {
        events.emit(Unregistered)
        values?.clear()
    }
}

val World.members: Boolean
    get() = this["members", false]