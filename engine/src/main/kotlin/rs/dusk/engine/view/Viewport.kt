package rs.dusk.engine.view

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.utility.inject
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
data class Viewport(
    var radius: Int = 15,
    val players: TrackingSet<Player> = TrackingSet(),
    val npcs: TrackingSet<NPC> = TrackingSet()
)

val viewportModule = module {
    single(createdAtStart = true) { ViewportSystem(get()) }
}

class ViewportSystem(tasks: EngineTasks) : Runnable {

    val set: Deque<Deferred<Unit>> = LinkedList()
    val players: Players by inject()
    val npcs: NPCs by inject()

    init {
        tasks.add(this)
    }

    override fun run() = runBlocking {
        players.forEach {
            val update = update(it, players, npcs)
            set.push(update)
        }
        while (set.isNotEmpty()) {
            set.poll().await()
        }
    }

    fun update(player: Player, players: Players, npcs: NPCs) = GlobalScope.async {
        val tile = player.tile
        val plane = tile.plane
        val port = player.viewport

        port.players.flip()
        port.npcs.flip()

        for ((sx, sy) in Spiral.STEPS[port.radius]) {
            val x = tile.x + sx
            val y = tile.y + sy
            val hash = y + (x shl 14) + (plane shl 28)
            val p = players[hash]
            val n = npcs[hash]
            if (p != null) {
                port.players.update(p)
            }
            if (n != null) {
                port.npcs.update(n)
            }
        }
    }
}