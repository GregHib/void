package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Player
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PostUpdateTask(tasks: EngineTasks) : ParallelEngineTask(tasks, -1) {

    private val logger = InlineLogger()
    val players: Players by inject()
    val npcs: NPCs by inject()

    override fun run() {
        players.forEach { player ->
            defers.add(updatePlayer(player))
        }
//        npcs.forEach { npc ->
//            defers.add(update(npc.changes, npc.movement.delta))
//        }
        val took = measureTimeMillis {
            super.run()
        }
        if (took > 0) {
            logger.info { "Update calculation took ${took}ms" }
        }
    }

    fun updatePlayer(player: Player) = GlobalScope.async {
        player.viewport.players.update()
    }

}