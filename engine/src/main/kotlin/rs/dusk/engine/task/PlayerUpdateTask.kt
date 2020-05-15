package rs.dusk.engine.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.send
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.model.entity.index.LocalChange
import rs.dusk.engine.model.entity.index.RegionChange
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.world.RegionPlane
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.view.PlayerTrackingSet
import rs.dusk.engine.view.Viewport
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
class PlayerUpdateTask : ParallelEngineTask() {

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

        val message = viewport.message
        val (writer, updates) = message

        processLocals(writer, updates, entities, viewport, true)
        processLocals(writer, updates, entities, viewport, false)
        processGlobals(writer, updates, entities, viewport, true)
        processGlobals(writer, updates, entities, viewport, false)

        player.send(message)
    }

    fun processLocals(
        sync: Writer,
        updates: Writer,
        set: PlayerTrackingSet,
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
            val updateType = if (remove) LocalChange.Update else player.change

            if (updateType == null) {
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
            sync.writeBits(2, updateType.id)

            if (remove) {
                encodeRegion(sync, set, player)
                continue
            }

            when (updateType) {
                LocalChange.Walk, LocalChange.Run ->
                    sync.writeBits(updateType.id + 2, player.changeValue)
                LocalChange.Tele -> {
                    sync.writeBits(1, false)// Exit teleport support not needed
                    sync.writeBits(12, player.changeValue)
                }
                else -> {
                }
            }
            updates.writeBytes(player.visuals.update ?: continue)

        }

        if (skip > -1) {
            writeSkip(sync, skip)
        }

        sync.finishBitAccess()
    }

    fun processGlobals(
        sync: Writer,
        updates: Writer,
        set: PlayerTrackingSet,
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

            if (set.local.contains(player)) {
                continue
            }

            viewport.setIdle(index)

            if (player == null) {
                skip++
                continue
            }

            if (!set.add.contains(player)) {
                skip++
                continue
            }

            if (skip > -1) {
                writeSkip(sync, skip)
                skip = -1
            }

            sync.writeBits(1, true)
            sync.writeBits(2, 0)
            encodeRegion(sync, set, player)
            sync.writeBits(6, player.tile.x and 0x3f)
            sync.writeBits(6, player.tile.y and 0x3f)
            sync.writeBits(1, true)
            updates.writeBytes(player.visuals.addition ?: continue)
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

    fun encodeRegion(sync: Writer, set: PlayerTrackingSet, player: Player) {
        val delta = player.tile.delta(set.lastSeen[player] ?: Tile.EMPTY)
        val change = calculateRegionUpdate(delta.regionPlane)
        sync.writeBits(1, change != RegionChange.Update)
        if (change != RegionChange.Update) {
            val value = calculateRegionValue(change, delta.regionPlane)
            sync.writeBits(2, change.id)
            when (change) {
                RegionChange.Height -> sync.writeBits(2, value)
                RegionChange.Local -> sync.writeBits(5, value)
                RegionChange.Global -> sync.writeBits(18, value)
                else -> {
                }
            }
        }
    }

    fun calculateRegionUpdate(delta: RegionPlane) = when {
        delta.x == 0 && delta.y == 0 && delta.plane == 0 -> RegionChange.Update
        delta.x == 0 && delta.y == 0 && delta.plane != 0 -> RegionChange.Height
        delta.x == -1 || delta.y == -1 || delta.x == 1 || delta.y == 1 -> RegionChange.Local
        else -> RegionChange.Global
    }

    fun calculateRegionValue(change: RegionChange, delta: RegionPlane) = when (change) {
        RegionChange.Height -> delta.plane
        RegionChange.Local -> (getMovementIndex(delta) and 0x7) or (delta.plane shl 3)
        RegionChange.Global -> (delta.y and 0xff) or (delta.x and 0xff shl 8) or (delta.plane shl 16)
        else -> -1
    }

    companion object {
        private val REGION_X = intArrayOf(-1, 0, 1, -1, 1, -1, 0, 1)
        private val REGION_Y = intArrayOf(1, 1, 1, 0, 0, -1, -1, -1)

        /**
         * Index of movement direction
         * |07|06|05|
         * |04|PP|03|
         * |02|01|00|
         */
        fun getMovementIndex(delta: RegionPlane): Int {
            for (i in REGION_X.indices) {
                if (REGION_X[i] == delta.x && REGION_Y[i] == delta.y) {
                    return i
                }
            }
            return -1
        }
    }
}