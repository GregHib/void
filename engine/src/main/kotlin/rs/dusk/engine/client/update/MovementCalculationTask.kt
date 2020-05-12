package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.Changes.Companion.RUN
import rs.dusk.engine.model.entity.index.Changes.Companion.TELE
import rs.dusk.engine.model.entity.index.Changes.Companion.UPDATE
import rs.dusk.engine.model.entity.index.Changes.Companion.WALK
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.update.visual.player.MovementType.Companion.TELEPORT
import rs.dusk.engine.model.entity.index.update.visual.player.movementType
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
            player.visuals.update != null -> UPDATE
            else -> -1
        }

        changes.localValue = when (changes.localUpdate) {
            WALK, RUN -> movement.direction
            TELE -> (delta.y and 0x1f) or (delta.x and 0x1f shl 5) or (delta.plane and 0x3 shl 10)
            else -> -1
        }
    }

    fun updateNPC(npc: NPC) = GlobalScope.async {
        val changes = npc.changes
        val movement = npc.movement
        val delta = movement.delta

        changes.localUpdate = when {
            delta.id != 0 && movement.direction != -1 -> WALK// TODO walk/run
            npc.visuals.update != null -> UPDATE
            else -> -1
        }

        changes.localValue = when (changes.localUpdate) {
            WALK -> getNpcMoveDirection(Direction.NORTH)// Walk direction
            RUN -> getNpcMoveDirection(Direction.NORTH)// Run direction
            else -> -1
        }
    }

    private val MOVE_X = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)
    private val MOVE_Y = intArrayOf(1, 1, 0, -1, -1, -1, 0, 1)

    fun getNpcMoveDirection(direction: Direction): Int {
        return if (direction == Direction.NONE) {
            -1
        } else {
            getNpcMoveDirection(direction.deltaX, direction.deltaY)
        }
    }

    private fun getNpcMoveDirection(dx: Int, dy: Int): Int {
        MOVE_X.forEachIndexed { i, x ->
            if (close(x, dx) && close(MOVE_Y[i], dy)) {
                return i
            }
        }
        return -1
    }

    private fun close(type: Int, value: Int): Boolean {
        return when (type) {
            -1 -> value < 0
            1 -> value > 0
            else -> value == 0
        }
    }

}