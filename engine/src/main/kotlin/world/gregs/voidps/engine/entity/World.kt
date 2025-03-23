package world.gregs.voidps.engine.entity

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.component.KoinComponent
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.loadNpcSpawns
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.ItemSpawns
import world.gregs.voidps.engine.entity.item.floor.loadItemSpawns
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.loadObjectSpawns
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.get
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

    val members: Boolean
        get() = Settings["world.members", false]

    fun start(files: Map<String, List<String>>) {
        loadItemSpawns(get<FloorItems>(), get<ItemSpawns>(), files.getOrDefault(Settings["spawns.items"], emptyList()))
        loadObjectSpawns(get<GameObjects>(), files.getOrDefault(Settings["spawns.objects"], emptyList()))
        loadNpcSpawns(get<NPCs>(), files.getOrDefault(Settings["spawns.npcs"], emptyList()))
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