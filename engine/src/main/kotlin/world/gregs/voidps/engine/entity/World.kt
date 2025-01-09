package world.gregs.voidps.engine.entity

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.component.KoinComponent
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.timer.TimerQueue
import world.gregs.voidps.engine.timer.Timers
import world.gregs.voidps.type.Tile
import java.util.concurrent.ConcurrentHashMap

const val MAX_PLAYERS = 0x800 // 2048
const val MAX_NPCS = 0x8000 // 32768

object World : Entity, Variable, EventDispatcher, Runnable, KoinComponent {
    override var tile = Tile.EMPTY

    override val variables = Variables(this)
    private val logger = InlineLogger()

    val id: Int
        get() = Settings["world", 16]
    val name: String
        get() = "World $id"
    val members: Boolean
        get() = Settings["members", false]

    fun start() {
        emit(Spawn)
    }

    val timers: Timers = TimerQueue(this)

    private val actions = ConcurrentHashMap<String, Pair<Int, () -> Unit>>()

    fun queue(name: String, initialDelay: Int = 0, block: () -> Unit) {
        actions[name] = (GameLoop.tick + initialDelay) to block
    }

    override fun run() {
        timers.run()
        val iterator = actions.iterator()
        while (iterator.hasNext()) {
            val (_, pair) = iterator.next()
            val (tick, block) = pair
            if (GameLoop.tick <= tick) {
                continue
            }
            iterator.remove()
            try {
                block.invoke()
            } catch (e: Exception) {
                logger.error(e) { "Error in world action!" }
            }
        }
    }

    fun clearQueue(name: String) {
        actions.remove(name)
    }

    fun clear() {
        timers.clearAll()
        for ((_, block) in actions.values) {
            block.invoke()
        }
        actions.clear()
    }

    fun shutdown() {
        emit(Despawn)
    }
}