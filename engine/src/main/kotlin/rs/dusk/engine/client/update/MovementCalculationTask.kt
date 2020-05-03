package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Changes.Companion.ADJACENT_REGION
import rs.dusk.engine.entity.model.Changes.Companion.GLOBAL_REGION
import rs.dusk.engine.entity.model.Changes.Companion.HEIGHT
import rs.dusk.engine.entity.model.Changes.Companion.NONE
import rs.dusk.engine.entity.model.Changes.Companion.RUN
import rs.dusk.engine.entity.model.Changes.Companion.TELE
import rs.dusk.engine.entity.model.Changes.Companion.WALK
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.visuals.player.MovementType.Companion.TELEPORT
import rs.dusk.engine.entity.model.visual.visuals.player.movementType
import rs.dusk.engine.model.Direction
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
        val moveType = player.movementType
        val delta = movement.delta
        val region = delta.region

        changes.regionUpdate = when {
            region.id == 0 && delta.plane == 0 -> NONE
            region.id == 0 && delta.plane != 0 -> HEIGHT
            region.x == -1 || region.y == -1 || region.x == 1 || region.y == 1 -> ADJACENT_REGION
            else -> GLOBAL_REGION
        }

        changes.regionValue = when (changes.regionUpdate) {
            HEIGHT -> delta.plane
            ADJACENT_REGION -> (delta.plane shl 3) or (getDirection(region.x, region.y) and 0x7)
            GLOBAL_REGION -> (region.y and 0xff) or (region.x and 0xff shl 8) or (delta.plane shl 16)
            else -> -1
        }

        changes.localUpdate = when {
            delta.id != 0 && movement.direction != -1 && movement.run -> RUN
            delta.id != 0 && movement.direction != -1 && !movement.run -> WALK
            delta.id != 0 && moveType == TELEPORT -> TELE
            player.visuals.update != null -> NONE
            else -> -1
        }

        changes.localValue = when (changes.localUpdate) {
            WALK, RUN -> movement.direction
            TELE -> (delta.y and 0x1f) or (delta.x and 0x1f shl 5) or (delta.plane and 0x3 shl 10)
            else -> -1
        }
    }

    fun getDirection(deltaX: Int, deltaY: Int): Int {
        return Direction.fromDelta(deltaX, deltaY)?.value ?: -1
    }

}