package rs.dusk.engine.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.LocalChange
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.npc.NPCMoveType
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 15, 2020
 */
class NPCMovementTask : ParallelEngineTask() {

    private val logger = InlineLogger()
    val npcs: NPCs by inject()

    override fun run() {
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

    fun updateNPC(npc: NPC) = GlobalScope.async {
        val movement = npc.movement
        val delta = movement.delta

        npc.change = when {
            delta.id != 0 && movement.walkStep != Direction.NONE && npc.movementType == NPCMoveType.Crawl -> LocalChange.Crawl
            delta.id != 0 && movement.runStep != Direction.NONE -> LocalChange.Run
            delta.id != 0 && movement.walkStep != Direction.NONE -> LocalChange.Walk
            delta.id != 0 -> LocalChange.Tele
            npc.visuals.update != null -> LocalChange.Update
            else -> null
        }

        if (npc.change == LocalChange.Run || npc.change == LocalChange.Walk || npc.change == LocalChange.Crawl) {
            npc.walkDirection = Direction.clockwise.indexOf(movement.walkStep)
        }
        if (npc.change == LocalChange.Run) {
            npc.runDirection = Direction.clockwise.indexOf(movement.runStep)
        }
    }

}