package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.task.player.PlayerChangeTask.Companion.getWalkIndex
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerTrackingSet
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.PlayerVisuals
import world.gregs.voidps.engine.entity.character.update.RegionChange
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.region.RegionPlane
import world.gregs.voidps.network.encode.updatePlayers

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

        processLocals(writer, updates, players, viewport, true)
        processLocals(writer, updates, players, viewport, false)
        processGlobals(writer, updates, players, viewport, true)
        processGlobals(writer, updates, players, viewport, false)

        player.client?.updatePlayers(writer, updates)
        player.client?.flush()
        viewport.shift()
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
        var player: Player
        sync.startBitAccess()

        for (i in set.indices) {
            index = set.locals[i]

            if (viewport.isIdle(index) == active) {
                continue
            }
            player = players.indexed(index)!!

            val remove = set.remove(player.index)
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
            sync.writeBits(1, player.visuals.flag != 0 && !remove)
            sync.writeBits(2, updateType.id)

            if (remove) {
                encodeRegion(sync, viewport, player)
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
            encodeVisuals(updates, player.visuals, player.visuals.flag, encoders)
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

            if (set.local(index)) {
                continue
            }

            if (viewport.isActive(index) == active) {
                continue
            }

            player = players.indexed(index)

            viewport.setIdle(index)

            if (player == null) {
                skip++
                continue
            }

            if (!set.add(index)) {
                skip++
                continue
            }

            if (skip > -1) {
                writeSkip(sync, skip)
                skip = -1
            }

            sync.writeBits(1, true)
            sync.writeBits(2, 0)
            encodeRegion(sync, viewport, player)
            sync.writeBits(6, player.tile.x and 0x3f)
            sync.writeBits(6, player.tile.y and 0x3f)
            sync.writeBits(1, initialFlag != 0)
            encodeVisuals(updates, player.visuals, initialFlag, initialEncoders)
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

    fun encodeRegion(sync: Writer, viewport: Viewport, player: Player): Boolean {
        val delta = player.tile.regionPlane.delta(RegionPlane(viewport.lastSeen[player.index]))
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
            viewport.lastSeen[player.index] = player.tile.regionPlane.id
            return true
        }
        return false
    }

    fun calculateRegionUpdate(delta: Delta) = when {
        delta.x == 0 && delta.y == 0 && delta.plane == 0 -> RegionChange.Update
        delta.x == 0 && delta.y == 0 && delta.plane != 0 -> RegionChange.Height
        delta.x == -1 || delta.y == -1 || delta.x == 1 || delta.y == 1 -> RegionChange.Local
        else -> RegionChange.Global
    }

    fun calculateRegionValue(change: RegionChange, delta: Delta) = when (change) {
        RegionChange.Height -> delta.plane
        RegionChange.Local -> (getWalkIndex(delta) and 0x7) or (delta.plane shl 3)
        RegionChange.Global -> (delta.y and 0xff) or (delta.x and 0xff shl 8) or (delta.plane shl 16)
        else -> -1
    }

    private fun encodeVisuals(updates: Writer, visuals: PlayerVisuals, flag: Int, encoders: List<VisualEncoder<PlayerVisuals>>) {
        if (flag == 0) {
            return
        }
        writeFlag(updates, flag)
        for (encoder in encoders) {
            if (!visuals.flagged(encoder.mask)) {
                continue
            }
            encoder.encode(updates, visuals)
        }
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
}