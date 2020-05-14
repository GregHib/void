package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.send
import rs.dusk.engine.client.update.encode.player.FaceEncoder.Companion.getFaceDirection
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.index.Changes.Companion.RUN
import rs.dusk.engine.model.entity.index.Changes.Companion.WALK
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.update.visual.npc.getTurn
import rs.dusk.engine.view.TrackingSet
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 12, 2020
 */
class NPCUpdateTask : ParallelEngineTask() {

    private val logger = InlineLogger()
    val players: Players by inject()
    val sessions: Sessions by inject()

    override fun run() {
        players.forEach {
            if (sessions.contains(it)) {
                defers.add(update(it))
            }
        }

        val took = measureTimeMillis {
            super.run()
        }

        if (took > 0) {
            logger.info { "NPC update took ${took}ms" }
        }
    }

    fun update(player: Player) = GlobalScope.async {
        val viewport = player.viewport
        val entities = viewport.npcs

        val message = viewport.npcMessage
        val (writer, updates) = message

        processLocals(writer, updates, entities)
        processAdditions(writer, updates, player, entities)

        player.send(message)
    }

    fun processLocals(
        sync: Writer,
        updates: Writer,
        set: TrackingSet<NPC>
    ) {
        sync.startBitAccess()
        sync.writeBits(8, set.current.size)
        for (npc in set.current) {
            val remove = set.remove.contains(npc)
            val updateType = if (remove) 0 else npc.changes.localUpdate

            if (updateType == -1) {
                sync.writeBits(1, false)
                continue
            }

            sync.writeBits(1, true)
            sync.writeBits(2, updateType)

            when (updateType) {
                WALK, RUN -> {
                    val value = npc.changes.localValue
                    var run = true
                    if (run) {
                        sync.writeBits(1, 1)
                    }
                    sync.writeBits(3, value)//Walk direction
                    if (run) {
                        sync.writeBits(3, value)//Run direction
                    }
                    sync.writeBits(1, npc.visuals.update != null)
                }
            }
            if (!remove) {
                updates.writeBytes(npc.visuals.update ?: continue)
            }
        }

        sync.finishBitAccess()
    }

    fun processAdditions(
        sync: Writer,
        updates: Writer,
        client: Player,
        set: TrackingSet<NPC>
    ) {
        for (npc in set.add) {
            val delta = npc.tile.delta(client.tile)
            val regionChange = false// When tele
            val turn = npc.getTurn()
            val direction = getFaceDirection(turn.x, turn.y)//TODO move into [Turn]

            sync.writeBits(15, npc.index)
            sync.writeBits(3, (direction shr 11) - 4)
            sync.writeBits(1, npc.visuals.update != null)
            sync.writeBits(5, delta.y + if (delta.y < 15) 32 else 0)
            sync.writeBits(2, npc.tile.plane)
            sync.writeBits(15, npc.id)
            sync.writeBits(5, delta.x + if (delta.x < 15) 32 else 0)
            sync.writeBits(1, regionChange)
            updates.writeBytes(npc.visuals.addition ?: continue)
        }
        sync.writeBits(15, 32767)
        sync.finishBitAccess()
    }

}