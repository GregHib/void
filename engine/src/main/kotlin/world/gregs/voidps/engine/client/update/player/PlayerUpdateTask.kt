package world.gregs.voidps.engine.client.update.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.view.PlayerTrackingSet
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.network.login.protocol.encode.updatePlayers
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.APPEARANCE_MASK
import world.gregs.voidps.type.Delta
import kotlin.math.abs

class PlayerUpdateTask(
    private val players: Players,
    private val encoders: List<VisualEncoder<PlayerVisuals>>,
) {

    private val initialEncoders = encoders.filter { it.initial }
    private val initialFlag = initialEncoders.sumOf { it.mask }

    fun run(player: Player) {
        val viewport = player.viewport ?: return
        val players = viewport.players

        val writer = viewport.playerChanges
        val updates = viewport.playerUpdates

        processLocals(player, writer, updates, players, viewport, true)
        processLocals(player, writer, updates, players, viewport, false)
        processGlobals(player, writer, updates, players, viewport, true)
        processGlobals(player, writer, updates, players, viewport, false)

        player.client?.updatePlayers(writer, updates)
        player.client?.flush()
        writer.position(0)
        updates.position(0)
    }

    fun processLocals(
        client: Player,
        sync: Writer,
        updates: Writer,
        set: PlayerTrackingSet,
        viewport: Viewport,
        active: Boolean,
    ) {
        var skip = -1
        var index: Int
        var player: Player
        var flag: Int
        var updateType: LocalChange
        sync.startBitAccess()
        for (i in 0 until set.localCount) {
            index = set.locals[i]

            if (viewport.isIdle(index) == active) {
                continue
            }
            player = players.indexed(index)!!

            flag = updateFlag(updates, player, set)
            updateType = localChange(updates, player, client, viewport, flag)

            if (updateType == LocalChange.None) {
                skip++
                viewport.setIdle(index)
                continue
            }

            if (skip > -1) {
                writeSkip(sync, skip)
                skip = -1
            }
            sync.writeBits(1, true)
            sync.writeBits(1, flag != 0 && updateType != LocalChange.Remove)
            sync.writeBits(2, updateType.id)

            if (updateType == LocalChange.Remove) {
                set.remove(index)
                encodeRegion(sync, viewport, player)
                viewport.seen(player)
                continue
            }

            encodeMovement(updateType, sync, viewport, player)
            encodeVisuals(updates, flag, player, client, set, encoders)
        }

        if (skip > -1) {
            writeSkip(sync, skip)
        }

        sync.stopBitAccess()
    }

    private fun encodeMovement(updateType: LocalChange, sync: Writer, viewport: Viewport, player: Player) {
        when (updateType) {
            LocalChange.Walk -> sync.writeBits(3, getWalkIndex(viewport.delta(player)))
            LocalChange.Run -> sync.writeBits(4, getRunIndex(viewport.delta(player)))
            LocalChange.Tele -> {
                val delta = viewport.delta(player)
                val local = abs(delta.x) <= viewport.radius && abs(delta.y) <= viewport.radius
                sync.writeBits(1, !local)
                if (local) {
                    sync.writeBits(12, (delta.y and 0x1f) or (delta.x and 0x1f shl 5) or (delta.level and 0x3 shl 10))
                } else {
                    sync.writeBits(30, (delta.y and 0x3fff) + (delta.x and 0x3fff shl 14) + (delta.level and 0x3 shl 28))
                }
            }
            else -> return
        }
        viewport.seen(player)
    }

    private fun encodeVisuals(updates: Writer, flag: Int, player: Player, client: Player, set: PlayerTrackingSet, encoders: List<VisualEncoder<PlayerVisuals>>) {
        if (flag == 0) {
            return
        }
        writeFlag(updates, flag)
        for (encoder in encoders) {
            if (flag and encoder.mask == 0) {
                continue
            }
            encoder.encode(updates, player.visuals, client.index)
        }
        if (flag and APPEARANCE_MASK != 0) {
            set.updateAppearance(player)
        }
    }

    /**
     * Adds or removes the [APPEARANCE_MASK] from [PlayerVisuals.flag] when
     * [PlayerTrackingSet.needsAppearanceUpdate] or nearing the [MAX_PACKET_SIZE].
     */
    private fun updateFlag(updates: Writer, player: Player, set: PlayerTrackingSet): Int {
        val visuals = player.visuals
        if (updates.position() + visuals.appearance.length >= MAX_UPDATE_SIZE) {
            return visuals.flag and APPEARANCE_MASK.inv()
        }
        if (set.needsAppearanceUpdate(player)) {
            return visuals.flag or APPEARANCE_MASK
        }
        return visuals.flag
    }

    /**
     * Calculate the type of update required for a local [player]
     * Note: movement is calculated from [Viewport.lastSeen] so players stay in
     * sync even if an update is skipped
     */
    private fun localChange(updates: Writer, player: Player, client: Player, viewport: Viewport, flag: Int): LocalChange {
        if (player.client?.disconnected == true || !player.tile.within(client.tile, viewport.radius)) {
            return LocalChange.Remove
        }
        if (updates.position() >= MAX_UPDATE_SIZE) {
            return LocalChange.None
        }

        val delta = viewport.delta(player)
        if (delta == Delta.EMPTY) {
            return if (flag != 0) LocalChange.Update else LocalChange.None
        }

        if (player.visuals.walkStep != -1) {
            if (player.visuals.runStep != -1 && getRunIndex(delta) != -1) {
                return LocalChange.Run
            }
            if (getWalkIndex(delta) != -1) {
                return LocalChange.Walk
            }
        }
        return LocalChange.Tele
    }

    fun processGlobals(
        client: Player,
        sync: Writer,
        updates: Writer,
        set: PlayerTrackingSet,
        viewport: Viewport,
        active: Boolean,
    ) {
        var skip = -1
        var index: Int
        var player: Player?
        sync.startBitAccess()
        for (i in 0 until set.globalCount) {
            index = set.globals[i]

            if (viewport.isActive(index) == active) {
                continue
            }

            player = players.indexed(index)
            viewport.setIdle(index)
            if (player == null) {
                skip++
                continue
            }

            if (!add(player, client, viewport, updates, sync)) {
                skip++
                continue
            }

            if (skip > -1) {
                writeSkip(sync, skip)
                skip = -1
            }

            val appearance = set.needsAppearanceUpdate(player)
            set.add(index)
            sync.writeBits(1, true)
            sync.writeBits(2, 0)
            encodeRegion(sync, viewport, player)
            viewport.seen(player)
            sync.writeBits(6, player.tile.x and 0x3f)
            sync.writeBits(6, player.tile.y and 0x3f)
            sync.writeBits(1, appearance)
            if (appearance) {
                encodeVisuals(updates, initialFlag, player, client, set, initialEncoders)
            }
        }
        if (skip > -1) {
            writeSkip(sync, skip)
        }
        sync.stopBitAccess()
    }

    /**
     * Check if a local [player] should be added to the local players list
     * @return true when within [Viewport.radius] and packet has enough room
     */
    private fun add(player: Player, client: Player, viewport: Viewport, updates: Writer, sync: Writer): Boolean = player.client?.disconnected != true &&
        player.tile.within(client.tile, viewport.radius) &&
        updates.position() < MAX_UPDATE_SIZE &&
        sync.position() < MAX_SYNC_SIZE

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

    fun encodeRegion(sync: Writer, viewport: Viewport, player: Player): Boolean {
        val delta = player.tile.regionLevel.delta(viewport.lastSeen(player).regionLevel)
        val change = calculateRegionUpdate(delta)
        sync.writeBits(1, change != RegionChange.None)
        if (change == RegionChange.None) {
            return false
        }
        sync.writeBits(2, change.id)
        when (change) {
            RegionChange.Height -> sync.writeBits(2, delta.level)
            RegionChange.Local -> sync.writeBits(5, (getWalkIndex(delta) and 0x7) or (delta.level shl 3))
            RegionChange.Global -> sync.writeBits(18, (delta.y and 0xff) or (delta.x and 0xff shl 8) or (delta.level shl 16))
            else -> return false
        }
        return true
    }

    fun calculateRegionUpdate(delta: Delta): RegionChange = when {
        delta.x == 0 && delta.y == 0 && delta.level == 0 -> RegionChange.None
        delta.x == 0 && delta.y == 0 && delta.level != 0 -> RegionChange.Height
        delta.x >= -1 && delta.x <= 1 && delta.y >= -1 && delta.y <= 1 -> RegionChange.Local
        else -> RegionChange.Global
    }

    fun writeFlag(writer: Writer, dataFlag: Int) {
        var flag = dataFlag

        if (flag >= 0x100) {
            flag = flag or 0x40
        }
        if (flag >= 0x10000) {
            flag = flag or 0x4000
        }
        writer.writeByte(flag)

        if (flag >= 0x100) {
            writer.writeByte(flag shr 8)
        }
        if (flag >= 0x10000) {
            writer.writeByte(flag shr 16)
        }
    }

    private sealed class LocalChange(val id: Int) {
        data object None : LocalChange(-1)
        data object Update : LocalChange(0)
        data object Remove : LocalChange(0)
        data object Walk : LocalChange(1)
        data object Run : LocalChange(2)
        data object Tele : LocalChange(3)
    }

    sealed class RegionChange(val id: Int) {
        data object None : RegionChange(-1)
        data object Height : RegionChange(1)
        data object Local : RegionChange(2)
        data object Global : RegionChange(3)
    }

    companion object {

        private const val MAX_PACKET_SIZE = 7500
        private const val MAX_SYNC_SIZE = 2500
        private const val MAX_UPDATE_SIZE = MAX_PACKET_SIZE - MAX_SYNC_SIZE - 100

        /**
         * Index of two movement directions
         * |11|12|13|14|15|
         * |09|--|--|--|10|
         * |07|--|PP|--|08|
         * |05|--|--|--|06|
         * |00|01|02|03|04|
         */
        fun getRunIndex(delta: Delta): Int = when {
            delta.x == -2 && delta.y == -2 -> 0
            delta.x == -1 && delta.y == -2 -> 1
            delta.x == 0 && delta.y == -2 -> 2
            delta.x == 1 && delta.y == -2 -> 3
            delta.x == 2 && delta.y == -2 -> 4
            delta.x == -2 && delta.y == -1 -> 5
            delta.x == 2 && delta.y == -1 -> 6
            delta.x == -2 && delta.y == 0 -> 7
            delta.x == 2 && delta.y == 0 -> 8
            delta.x == -2 && delta.y == 1 -> 9
            delta.x == 2 && delta.y == 1 -> 10
            delta.x == -2 && delta.y == 2 -> 11
            delta.x == -1 && delta.y == 2 -> 12
            delta.x == 0 && delta.y == 2 -> 13
            delta.x == 1 && delta.y == 2 -> 14
            delta.x == 2 && delta.y == 2 -> 15
            else -> -1
        }

        /**
         * Index of movement direction
         * |05|06|07|
         * |03|PP|04|
         * |00|01|02|
         */
        fun getWalkIndex(delta: Delta): Int = when {
            delta.x == -1 && delta.y == -1 -> 0
            delta.x == 0 && delta.y == -1 -> 1
            delta.x == 1 && delta.y == -1 -> 2
            delta.x == -1 && delta.y == 0 -> 3
            delta.x == 1 && delta.y == 0 -> 4
            delta.x == -1 && delta.y == 1 -> 5
            delta.x == 0 && delta.y == 1 -> 6
            delta.x == 1 && delta.y == 1 -> 7
            else -> -1
        }
    }
}
