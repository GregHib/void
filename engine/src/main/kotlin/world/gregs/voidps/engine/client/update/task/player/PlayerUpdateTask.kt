package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerTrackingSet
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.RegionChange
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.region.RegionPlane
import world.gregs.voidps.engine.tick.task.EntityTask
import world.gregs.voidps.network.encode.PlayerUpdateEncoder

/**
 * @author GregHib <greg@gregs.world>
 * @since April 26, 2020
 */
class PlayerUpdateTask(
    override val entities: Players,
    val sessions: Sessions,
    private val updateEncoder: PlayerUpdateEncoder
) : EntityTask<Player>(true) {

    override fun predicate(entity: Player): Boolean {
        return sessions.contains(entity)
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(player: Player) {
        val viewport = player.viewport
        val players = viewport.players

        val writer = viewport.playerChanges
        val updates = viewport.playerUpdates

        processLocals(writer, updates, players, viewport, true)
        processLocals(writer, updates, players, viewport, false)
        processGlobals(writer, updates, players, viewport, true)
        processGlobals(writer, updates, players, viewport, false)

        updateEncoder.encode(player, writer, updates)
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
                LocalChange.Tele, LocalChange.TeleGlobal -> {
                    val global = updateType == LocalChange.TeleGlobal
                    sync.writeBits(1, global)
                    val size = if (global) 30 else 12
                    sync.writeBits(size, player.changeValue)
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

            player = entities.getAtIndex(index)

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
        val delta = player.tile.regionPlane.delta(set.lastSeen[player]?.regionPlane ?: RegionPlane.EMPTY)
        val change = calculateRegionUpdate(delta)
        sync.writeBits(1, change != RegionChange.Update)
        if (change != RegionChange.Update) {
            val value = calculateRegionValue(change, delta)
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

    fun calculateRegionUpdate(delta: Delta) = when {
        delta.x == 0 && delta.y == 0 && delta.plane == 0 -> RegionChange.Update
        delta.x == 0 && delta.y == 0 && delta.plane != 0 -> RegionChange.Height
        delta.x == -1 || delta.y == -1 || delta.x == 1 || delta.y == 1 -> RegionChange.Local
        else -> RegionChange.Global
    }

    fun calculateRegionValue(change: RegionChange, delta: Delta) = when (change) {
        RegionChange.Height -> delta.plane
        RegionChange.Local -> (getMovementIndex(delta) and 0x7) or (delta.plane shl 3)
        RegionChange.Global -> (delta.y and 0xff) or (delta.x and 0xff shl 8) or (delta.plane shl 16)
        else -> -1
    }

    companion object {
        private val REGION_X = intArrayOf(-1, 0, 1, -1, 1, -1, 0, 1)
        private val REGION_Y = intArrayOf(-1, -1, -1, 0, 0, 1, 1, 1)

        /**
         * Index of movement direction
         * |05|06|07|
         * |03|PP|04|
         * |00|01|02|
         */
        fun getMovementIndex(delta: Delta): Int {
            for (i in REGION_X.indices) {
                if (REGION_X[i] == delta.x && REGION_Y[i] == delta.y) {
                    return i
                }
            }
            return -1
        }
    }
}