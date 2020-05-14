package rs.dusk.engine.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.LocalChange
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
        val movement = player.movement
        val delta = movement.delta

        movement.lastTile = player.tile

        player.change = when {
            delta.id != 0 && movement.runStep != Direction.NONE -> LocalChange.Run
            delta.id != 0 && movement.walkStep != Direction.NONE -> LocalChange.Walk
            delta.id != 0 && player.movementType == TELEPORT -> LocalChange.Tele
            player.visuals.update != null -> LocalChange.Update
            else -> null
        }

        player.changeValue = when (player.change) {
            LocalChange.Walk -> movement.walkStep.inverse().value
            LocalChange.Run -> getPlayerRunningDirection(
                movement.walkStep.deltaX + movement.runStep.deltaX,
                movement.walkStep.deltaY + movement.runStep.deltaY
            )
            LocalChange.Tele -> (delta.y and 0x1f) or (delta.x and 0x1f shl 5) or (delta.plane and 0x3 shl 10)
            else -> -1
        }
    }

    fun updateNPC(npc: NPC) = GlobalScope.async {
        val movement = npc.movement
        val delta = movement.delta

        npc.change = when {
            delta.id != 0 && movement.runStep != Direction.NONE -> LocalChange.Run
            delta.id != 0 && movement.walkStep != Direction.NONE -> LocalChange.Walk
            delta.id != 0 -> LocalChange.Tele// Tele
            npc.visuals.update != null -> LocalChange.Update
            else -> null
        }

        if (npc.change == LocalChange.Run || npc.change == LocalChange.Walk) {
            npc.walkDirection = getNpcMoveDirection(movement.walkStep)
            npc.runDirection = getNpcMoveDirection(movement.runStep)
        } else {
            npc.walkDirection = -1
            npc.runDirection = -1
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