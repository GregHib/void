package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.core.io.write.BufferWriter
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.client.send
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Changes
import rs.dusk.engine.entity.model.Changes.Companion.HEIGHT
import rs.dusk.engine.entity.model.Changes.Companion.LOCAL_REGION
import rs.dusk.engine.entity.model.Changes.Companion.NONE
import rs.dusk.engine.entity.model.Changes.Companion.OTHER_REGION
import rs.dusk.engine.entity.model.Changes.Companion.RUN
import rs.dusk.engine.entity.model.Changes.Companion.TELE
import rs.dusk.engine.entity.model.Changes.Companion.WALK
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.view.TrackingSet
import rs.dusk.network.rs.codec.game.encode.message.PlayerUpdateMessage
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
class PlayerUpdater(tasks: EngineTasks) : ParallelEngineTask(tasks) {

    val players: Players by inject()
    private val logger = InlineLogger()

    override fun run() {
        players.forEach {
            defers.add(update(it))
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

        writer.processLocals(updates, entities, player, true)
        writer.processLocals(updates, entities, player, false)
        writer.processGlobals(updates, entities, player, true)
        writer.processGlobals(updates, entities, player, false)

        viewport.shift()

        player.send(PlayerUpdateMessage(writer.buffer, updates.buffer))
    }

    fun Writer.processLocals(
        updates: Writer,
        set: TrackingSet<Player>,
        client: Player,
        active: Boolean
    ) {
        val viewport = client.viewport
        var skip = -1
        startBitAccess()

        var index: Int
        var changes: Changes
        for (player in set.current) {
            index = player.index

            if (viewport.isIdle(index) == active) {
                continue
            }

            changes = player.changes
            val remove = set.remove.contains(player)
            val updateType = if (remove) 0 else changes.localUpdate

            if (updateType == -1) {
                skip++
                viewport.setIdle(index)
                continue
            }

            if (skip > -1) {
                writeSkip(skip)
                skip = -1
            }

            writeBits(1, true)
            writeBits(1, updateType == 0 && !remove)
            writeBits(2, updateType)

            if (remove) {
                encodeRegion(changes)
            } else {
                val value = changes.localValue
                when (updateType) {
                    WALK, RUN -> writeBits(updateType + 2, value)
                    TELE -> {
                        writeBits(1, false)// Exit teleport support not needed
                        writeBits(12, value)
                    }
                }
                println("Write update $index")
                updates.writeBytes(player.visuals.update ?: continue)
            }
        }

        if (skip > -1) {
            writeSkip(skip)
        }

        finishBitAccess()
    }

    fun Writer.processGlobals(
        updates: Writer,
        set: TrackingSet<Player>,
        client: Player,
        active: Boolean
    ) {
        val viewport = client.viewport
        var skip = -1
        startBitAccess()
        var player: Player?
        var changes: Changes
        for (index in 1 until MAX_PLAYERS) {

            if (viewport.isActive(index) == active) {
                continue
            }

            player = players.getAtIndex(index)

            if (player != null && set.current.contains(player)) {
                continue
            }

            if (player == null) {
                skip++
                viewport.setIdle(index)
                continue
            }

            changes = player.changes
            val add = set.add.contains(player)
            val updateType = if (add) 0 else if (changes.regionValue != -1) changes.regionUpdate else -1

            if (updateType == -1) {
                skip++
                viewport.setIdle(index)
                continue
            }

            if (skip > -1) {
                writeSkip(skip)
                skip = -1
            }

            writeBits(1, true)
            writeBits(2, updateType)

            if (add) {
                encodeRegion(changes)
                writeBits(6, player.tile.x and 0x3f)
                writeBits(6, player.tile.y and 0x3f)
                writeBits(1, true)
                viewport.setIdle(index)
                println("Write $index ${player.visuals.base?.toList()}")
                updates.writeBytes(player.visuals.base ?: continue)
            }
        }
        if (skip > -1) {
            writeSkip(skip)
        }
        finishBitAccess()
    }

    fun Writer.writeSkip(skip: Int) {
        writeBits(1, 0)
        when {
            skip == 0 -> writeBits(2, 0)
            skip < 32 -> {
                writeBits(2, 1)
                writeBits(5, skip)
            }
            skip < 256 -> {
                writeBits(2, 2)
                writeBits(8, skip)
            }
            skip < 2048 -> {
                writeBits(2, 3)
                writeBits(11, skip)
            }
        }
    }

    fun Writer.encodeRegion(changes: Changes) {
        val change = changes.regionUpdate
        writeBits(1, change != NONE)
        if (change != NONE) {
            writeBits(2, change)
            val value = changes.regionValue
            when (change) {
                HEIGHT -> writeBits(2, value)
                LOCAL_REGION -> writeBits(5, value)
                OTHER_REGION -> writeBits(18, value)
            }
        }
    }
}