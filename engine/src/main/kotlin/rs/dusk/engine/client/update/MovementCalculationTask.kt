package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Changes.Companion.NONE
import rs.dusk.engine.entity.model.Changes.Companion.RUN
import rs.dusk.engine.entity.model.Changes.Companion.TELE
import rs.dusk.engine.entity.model.Changes.Companion.WALK
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.visuals.player.MovementType.Companion.TELEPORT
import rs.dusk.engine.entity.model.visual.visuals.player.movementType
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class MovementCalculationTask(tasks: EngineTasks) : ParallelEngineTask(tasks, 1) {

    private val logger = InlineLogger()
    val players: Players by inject()

    override fun run() {
        players.forEach { player ->
            defers.add(updatePlayer(player))
        }
        val took = measureTimeMillis {
            super.run()
        }
        if (took > 0) {
            logger.info { "Update calculation took ${took}ms" }
        }
    }

    fun updatePlayer(player: Player) = GlobalScope.async {
        val changes = player.changes
        val movement = player.movement
        val delta = movement.delta

        movement.lastTile = player.tile

        changes.localUpdate = when {
            delta.id != 0 && movement.direction != -1 -> player.movementType
            delta.id != 0 && player.movementType == TELEPORT -> TELE
            player.visuals.update != null -> NONE
            else -> -1
        }

        changes.localValue = when (changes.localUpdate) {
            WALK, RUN -> movement.direction
            TELE -> (delta.y and 0x1f) or (delta.x and 0x1f shl 5) or (delta.plane and 0x3 shl 10)
            else -> -1
        }
    }

}