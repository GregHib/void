package rs.dusk.engine.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.send
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.index.LocalChange
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.teleport
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
        val npcs = viewport.npcs

        val message = viewport.npcMessage
        val (writer, updates) = message

        processLocals(writer, updates, npcs)
        processAdditions(writer, updates, player, npcs)

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
            val change = if (remove) LocalChange.Tele else npc.change

            if (change == null) {
                sync.writeBits(1, false)
                continue
            }

            sync.writeBits(1, true)
            sync.writeBits(2, change.id)

            when (change) {
                LocalChange.Walk -> {
                    sync.writeBits(3, npc.walkDirection)
                    sync.writeBits(1, npc.visuals.update != null)
                }
                LocalChange.Crawl -> {
                    sync.writeBits(1, false)
                    sync.writeBits(3, npc.walkDirection)
                    sync.writeBits(1, npc.visuals.update != null)
                }
                LocalChange.Run -> {
                    sync.writeBits(1, true)
                    sync.writeBits(3, npc.walkDirection)
                    sync.writeBits(3, npc.runDirection)
                    sync.writeBits(1, npc.visuals.update != null)
                }
                else -> {
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
            val regionChange =
                npc.teleport// TODO test, teleport probably removed by now as add happens in the next tick
            val direction = npc.getTurn().direction

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