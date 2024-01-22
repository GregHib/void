package world.gregs.voidps.engine.entity

import org.koin.core.component.KoinComponent
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Tile
import java.util.*
import java.util.concurrent.ConcurrentHashMap

const val MAX_PLAYERS = 0x800 // 2048
const val MAX_NPCS = 0x8000 // 32768

object World : Entity, Variable, EventDispatcher, Runnable, KoinComponent {
    override var tile = Tile.EMPTY
    override val events: Events = Events(this)

    override val variables = Variables(events)

    var id = 0
        private set(value) {
            field = value
            name = "World $value"
        }
    var name: String = "World"
        private set
    var members: Boolean = false
        private set

    fun start(properties: Properties) {
        val members = properties.getProperty("members").toBoolean()
        val id = properties.getProperty("world").toInt()
        start(members, id)
    }

    fun start(members: Boolean = true, id: Int = 16) {
        this.members = members
        this.id = id
        val store: EventHandlerStore = get()
        store.populate(World)
        events.emit(Registered)
    }

    private val timers = ConcurrentHashMap<String, Pair<Int, () -> Unit>>()

    fun run(name: String, delay: Int, block: () -> Unit) {
        timers[name] = (GameLoop.tick + delay) to block
    }

    override fun run() {
        val iterator = timers.iterator()
        while (iterator.hasNext()) {
            val (_, pair) = iterator.next()
            val (tick, block) = pair
            if (GameLoop.tick <= tick) {
                continue
            }
            block.invoke()
            iterator.remove()
        }
    }

    fun stopTimer(name: String) {
        val (_, block) = timers.remove(name) ?: return
        block.invoke()
    }

    fun clearTimers() {
        for ((_, block) in timers.values) {
            block.invoke()
        }
        timers.clear()
    }

    fun shutdown() {
        events.emit(Unregistered)
    }
}