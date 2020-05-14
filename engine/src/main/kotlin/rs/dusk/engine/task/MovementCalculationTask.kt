package rs.dusk.engine.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.Changes.Companion.REMOVE
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
class MovementCalculationTask : ParallelEngineTask() {

    private val logger = InlineLogger()
    val players: Players by inject()
    val npcs: NPCs by inject()

    override fun run() {
        players.forEach { player ->
            defers.add(updatePlayer(player))
        }
        npcs.forEach { npc ->
            defers.add(updateNPC(npc))
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
            delta.id != 0 && movement.runStep != Direction.NONE -> RUN
            delta.id != 0 && movement.walkStep != Direction.NONE -> WALK
            delta.id != 0 && player.movementType == TELEPORT -> TELE
            player.visuals.update != null -> UPDATE
            else -> -1
        }

        changes.localValue = when (changes.localUpdate) {
            WALK -> movement.walkStep.inverse().value
            RUN -> getPlayerRunningDirection(
                movement.walkStep.deltaX + movement.runStep.deltaX,
                movement.walkStep.deltaY + movement.runStep.deltaY
            )
            TELE -> (delta.y and 0x1f) or (delta.x and 0x1f shl 5) or (delta.plane and 0x3 shl 10)
            else -> -1
        }
    }

    fun updateNPC(npc: NPC) = GlobalScope.async {
        val changes = npc.changes
        val movement = npc.movement
        val delta = movement.delta

        changes.localUpdate = when {
            delta.id != 0 && movement.runStep != Direction.NONE -> RUN
            delta.id != 0 && movement.walkStep != Direction.NONE -> WALK
            delta.id != 0 -> REMOVE// Tele
            npc.visuals.update != null -> UPDATE
            else -> -1
        }

        changes.localValue = when (changes.localUpdate) {
            WALK -> getNpcMoveDirection(Direction.NORTH)// Walk direction
            RUN -> getNpcMoveDirection(Direction.NORTH)// Run direction
            else -> -1
        }
    }

    val RUN_X = intArrayOf(-2, -1, 0, 1, 2, -2, 2, -2, 2, -2, 2, -2, -1, 0, 1, 2)
    val RUN_Y = intArrayOf(-2, -2, -2, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 2, 2, 2)

    fun getPlayerRunningDirection(dx: Int, dy: Int): Int {
        RUN_X.forEachIndexed { i, x ->
            if (dx == x && dy == RUN_Y[i]) {
                return i
            }
        }
        return -1
    }

    private val MOVE_X = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)
    private val MOVE_Y = intArrayOf(1, 1, 0, -1, -1, -1, 0, 1)

    fun getNpcMoveDirection(direction: Direction) = getNpcMoveDirection(direction.deltaX, direction.deltaY)

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