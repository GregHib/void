package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.core.io.write.BufferWriter
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.send
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Changes
import rs.dusk.engine.entity.model.Changes.Companion.ADJACENT_REGION
import rs.dusk.engine.entity.model.Changes.Companion.GLOBAL_REGION
import rs.dusk.engine.entity.model.Changes.Companion.HEIGHT
import rs.dusk.engine.entity.model.Changes.Companion.NONE
import rs.dusk.engine.entity.model.Changes.Companion.RUN
import rs.dusk.engine.entity.model.Changes.Companion.TELE
import rs.dusk.engine.entity.model.Changes.Companion.WALK
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.view.TrackingSet
import rs.dusk.engine.view.Viewport
import rs.dusk.network.rs.codec.game.encode.message.PlayerUpdateMessage
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
class PlayerUpdateTask(tasks: EngineTasks) : ParallelEngineTask(tasks) {

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
            logger.info { "Player update took ${took}ms" }
        }
    }

    fun update(player: Player) = GlobalScope.async {
        val viewport = player.viewport
        val entities = viewport.players

        val writer = BufferWriter()
        val updates = BufferWriter()

        processLocals(writer, updates, entities, viewport, true)
        processLocals(writer, updates, entities, viewport, false)
        processGlobals(writer, updates, entities, viewport, true)
        processGlobals(writer, updates, entities, viewport, false)

        player.send(PlayerUpdateMessage(writer.buffer, updates.buffer))
    }

    fun processLocals(
        sync: Writer,
        updates: Writer,
        set: TrackingSet<Player>,
        viewport: Viewport,
        active: Boolean
    ) {
        var skip = -1
        var index: Int
        sync.startBitAccess()
        for (player in set.current) {
            index = player.index

            if (viewport.isIdle(index) == active) {
                continue
            }

            val remove = set.remove.contains(player)
            val updateType = if (remove) 0 else player.changes.localUpdate

            if (updateType == -1) {
                skip++
                viewport.setIdle(index)
                continue
            }

            if (skip > -1) {
                writeSkip(sync, skip)
                skip = -1
            }

            sync.writeBits(1, true)
            sync.writeBits(1, player.visuals.update != null && !remove)
            sync.writeBits(2, updateType)

            if (remove) {
                encodeRegion(sync, player.changes)
            } else {
                val value = player.changes.localValue
                when (updateType) {
                    WALK, RUN -> sync.writeBits(updateType + 2, value)
                    TELE -> {
                        sync.writeBits(1, false)// Exit teleport support not needed
                        sync.writeBits(12, value)
                    }
                }
                updates.writeBytes(player.visuals.update ?: continue)
            }
        }

        if (skip > -1) {
            writeSkip(sync, skip)
        }

        sync.finishBitAccess()
    }

    fun processGlobals(
        sync: Writer,
        updates: Writer,
        set: TrackingSet<Player>,
        viewport: Viewport,
        active: Boolean
    ) {
        var skip = -1
        var player: Player?
        sync.startBitAccess()
        for (index in 1 until MAX_PLAYERS) {

            if (viewport.isActive(index) == active) {
                continue
            }

            player = players.getAtIndex(index)

            if (player == null) {
                skip++
                viewport.setIdle(index)
                continue
            }

            if (set.local.contains(player)) {
                continue
            }

            val add = set.add.contains(player)
            viewport.setIdle(index)

            if (!add) {
                skip++
                continue
            }


            if (skip > -1) {
                writeSkip(sync, skip)
                skip = -1
            }

            sync.writeBits(1, true)
            sync.writeBits(2, 0)

            if (add) {
                encodeRegion(sync, player.changes)
                sync.writeBits(6, player.tile.x and 0x3f)
                sync.writeBits(6, player.tile.y and 0x3f)
                sync.writeBits(1, true)
                val update = player.visuals.addition
                if (update != null) {
                    updates.writeBytes(update)
                }
            }
        }
        if (skip > -1) {
            writeSkip(sync, skip)
        }
        sync.finishBitAccess()
    }

    fun writeSkip(sync: Writer, skip: Int) {
        sync.writeBits(1, 0)
        when {
            skip == 0 -> sync.writeBits(2, 0)
            skip < 32 -> {
                sync.writeBits(2, 1)
                sync.writeBits(5, skip)
            }
            skip < 256 -> {
                sync.writeBits(2, 2)
                sync.writeBits(8, skip)
            }
            skip < 2048 -> {
                sync.writeBits(2, 3)
                sync.writeBits(11, skip)
            }
        }
    }

    fun encodeRegion(sync: Writer, changes: Changes) {
        val change = changes.regionUpdate
        sync.writeBits(1, change != NONE)
        if (change != NONE) {
            sync.writeBits(2, change)
            val value = changes.regionValue
            when (change) {
                HEIGHT -> sync.writeBits(2, value)
                ADJACENT_REGION -> sync.writeBits(5, value)
                GLOBAL_REGION -> sync.writeBits(18, value)
            }
        }
    }
}