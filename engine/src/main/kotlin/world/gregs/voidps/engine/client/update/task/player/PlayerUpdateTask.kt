package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerTrackingSet
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.encode.updatePlayers
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.APPEARANCE_MASK
import kotlin.math.abs

class PlayerUpdateTask(
    private val players: CharacterList<Player>,
    private val encoders: List<VisualEncoder<PlayerVisuals>>
) {

    private val initialEncoders = encoders.filter { it.initial }
    private val initialFlag = initialEncoders.sumOf { it.mask }

    fun run(player: Player) {
        val viewport = player.viewport
        val players = viewport.players

        val writer = viewport.playerChanges
        val updates = viewport.playerUpdates

        processLocals(player, writer, updates, players, viewport, true)
        processLocals(player, writer, updates, players, viewport, false)
        processGlobals(player, writer, updates, players, viewport, true)
        processGlobals(player, writer, updates, players, viewport, false)

        player.client?.updatePlayers(writer, updates)
        player.client?.flush()
    }

    fun processLocals(
        client: Player,
        sync: Writer,
        updates: Writer,
        set: PlayerTrackingSet,
        viewport: Viewport,
        active: Boolean
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
            updateType = localChange(updates, player, client, flag)

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
                continue
            }

            if (updateType is LocalChange.Movement) {
                sync.writeBits(updateType.id + 2, updateType.index)
                viewport.lastSeen[index] = player.tile.id
            } else if (updateType is LocalChange.Teleport) {
                sync.writeBits(1, updateType is LocalChange.TeleGlobal)
                sync.writeBits(updateType.size, updateType.changeValue)
                viewport.lastSeen[index] = player.tile.id
            }
            if (flag == 0) {
                continue
            }
            writeFlag(updates, flag)
            for (encoder in encoders) {
                if (flag and encoder.mask == 0) {
                    continue
                }
                encoder.encode(updates, player.visuals)
            }
            if (flag and APPEARANCE_MASK != 0) {
                set.updateAppearance(player)
            }
        }

        if (skip > -1) {
            writeSkip(sync, skip)
        }

        sync.finishBitAccess()
    }

    /**
     * Adds or removes the [APPEARANCE_MASK] from [PlayerVisuals.flag] when
     * [PlayerTrackingSet.needsAppearanceUpdate] or nearing the [MAX_PACKET_SIZE].
     */
    private fun updateFlag(updates: Writer, player: Player, set: PlayerTrackingSet): Int {
        val visuals = player.visuals
        if (visuals.flagged(APPEARANCE_MASK)) {
            if (!set.needsAppearanceUpdate(player) || updates.position() + visuals.appearance.length() >= MAX_UPDATE_SIZE) {
                return visuals.flag and APPEARANCE_MASK.inv()
            }
        } else if (set.needsAppearanceUpdate(player)) {
            return visuals.flag or APPEARANCE_MASK
        }
        return visuals.flag
    }

    /**
     * Calculate the type of update required for a local [player]
     * Note: movement is calculated from [Viewport.lastSeen] so players stay in
     * sync even if an update is skipped
     */
    private fun localChange(updates: Writer, player: Player, client: Player, flag: Int): LocalChange {
        val viewport = client.viewport
        if (player.client?.disconnected == true || !player.tile.within(client.tile, viewport.radius)) {
            return LocalChange.Remove
        }
        if (updates.position() >= MAX_UPDATE_SIZE) {
            return LocalChange.None
        }

        val delta = player.tile.delta(Tile(viewport.lastSeen[player.index]))
        if (delta == Delta.EMPTY) {
            return if (flag != 0) LocalChange.Update else LocalChange.None
        }

        val runIndex = getRunIndex(delta)
        if (runIndex != -1 && player.movement.runStep != Direction.NONE) {
            return LocalChange.Run(runIndex)
        }

        val walkIndex = getWalkIndex(delta)
        if (walkIndex != -1 && player.movement.walkStep != Direction.NONE) {
            return LocalChange.Walk(walkIndex)
        }

        if (abs(delta.x) <= viewport.radius && abs(delta.y) <= viewport.radius) {
            return LocalChange.Tele(delta)
        }
        return LocalChange.TeleGlobal(delta)

    }

    fun processGlobals(
        client: Player,
        sync: Writer,
        updates: Writer,
        set: PlayerTrackingSet,
        viewport: Viewport,
        active: Boolean
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

            if (!add(player, client, updates, sync)) {
                skip++
                continue
            }

            if (skip > -1) {
                writeSkip(sync, skip)
                skip = -1
            }
            val appearance = set.needsAppearanceUpdate(player)
            set.add(index)
            viewport.lastSeen[player.index] = player.tile.id
            sync.writeBits(1, true)
            sync.writeBits(2, 0)
            encodeRegion(sync, viewport, player)
            sync.writeBits(6, player.tile.x and 0x3f)
            sync.writeBits(6, player.tile.y and 0x3f)
            sync.writeBits(1, appearance)
            if (appearance) {
                writeFlag(updates, initialFlag)
                for (encoder in initialEncoders) {
                    encoder.encode(updates, player.visuals)
                }
                set.updateAppearance(player)
            }
        }
        if (skip > -1) {
            writeSkip(sync, skip)
        }
        sync.finishBitAccess()
    }

    /**
     * Check if a local [player] should be added to the local players list
     * @return true when within [Viewport.radius] and packet has enough room
     */
    private fun add(player: Player, client: Player, updates: Writer, sync: Writer): Boolean {
        return player.tile.within(client.tile, client.viewport.radius) &&
                updates.position() < MAX_UPDATE_SIZE &&
                sync.position() < MAX_SYNC_SIZE
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

    fun encodeRegion(sync: Writer, viewport: Viewport, player: Player): Boolean {
        val delta = player.tile.regionPlane.delta(Tile(viewport.lastSeen[player.index]).regionPlane)
        val change = calculateRegionUpdate(delta)
        sync.writeBits(1, change != RegionChange.None)
        if (change is RegionChange.Movement) {
            sync.writeBits(2, change.id)
            sync.writeBits(change.bits, change.value)
            return true
        }
        return false
    }

    fun calculateRegionUpdate(delta: Delta) = when {
        delta.x == 0 && delta.y == 0 && delta.plane == 0 -> RegionChange.None
        delta.x == 0 && delta.y == 0 && delta.plane != 0 -> RegionChange.Height(delta)
        delta.x == -1 || delta.y == -1 || delta.x == 1 || delta.y == 1 -> RegionChange.Local(delta)
        else -> RegionChange.Global(delta)
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
        object None : LocalChange(-1)
        object Update : LocalChange(0)
        object Remove : LocalChange(0)

        sealed class Movement(
            val index: Int,
            id: Int
        ) : LocalChange(id)

        class Walk(changeValue: Int) : Movement(changeValue, 1)
        class Run(changeValue: Int) : Movement(changeValue, 2)

        sealed class Teleport(
            val changeValue: Int,
            val size: Int
        ) : LocalChange(3)

        class Tele(delta: Delta) : Teleport((delta.y and 0x1f) or (delta.x and 0x1f shl 5) or (delta.plane and 0x3 shl 10), 12)
        class TeleGlobal(delta: Delta) : Teleport((delta.y and 0x3fff) + (delta.x and 0x3fff shl 14) + (delta.plane and 0x3 shl 28), 30)
    }

    sealed class RegionChange {
        object None : RegionChange()

        sealed class Movement(
            val id: Int,
            val bits: Int,
            val value: Int
        ) : RegionChange()

        class Height(delta: Delta) : Movement(1, 2, delta.plane)
        class Local(delta: Delta) : Movement(2, 5, (getWalkIndex(delta) and 0x7) or (delta.plane shl 3))
        class Global(delta: Delta) : Movement(3, 18, (delta.y and 0xff) or (delta.x and 0xff shl 8) or (delta.plane shl 16))
    }

    companion object {

        private const val MAX_PACKET_SIZE = 7500
        private const val MAX_SYNC_SIZE = 2500
        private const val MAX_UPDATE_SIZE = MAX_PACKET_SIZE - MAX_SYNC_SIZE - 100

        /**
         * Index of two combined movement directions
         * |11|12|13|14|15|
         * |09|  |  |  |10|
         * |07|  |PP|  |08|
         * |05|  |  |  |06|
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