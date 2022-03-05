package world.gregs.voidps.engine.client.update.task.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.task.player.PlayerChangeTask.Companion.getRunIndex
import world.gregs.voidps.engine.client.update.task.player.PlayerChangeTask.Companion.getWalkIndex
import world.gregs.voidps.engine.client.update.task.player.PlayerChangeTask.Companion.withinView
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.LocalChange
import world.gregs.voidps.engine.entity.character.RegionChange
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerTrackingSet
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.encode.updatePlayers
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.APPEARANCE_MASK
import world.gregs.voidps.network.visual.encode.player.AppearanceEncoder

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
        viewport.shift()
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
        var update: Boolean
        sync.startBitAccess()

        for (i in 0 until set.localPlayersIndexesCount) {
            index = set.localPlayersIndexes[i]

            if (viewport.isIdle(index) == active) {
                continue
            }
            player = players.indexed(index)!!

            val remove = player.client?.disconnected == true || !player.tile.within(client.tile, ViewportUpdating.VIEW_RADIUS)


            var flag = player.visuals.flag
            if(flag and APPEARANCE_MASK != 0 && player.visuals.appearance.hashCode() != set.appearanceHash[index]) {
                flag = flag or APPEARANCE_MASK
            }
            var updateType: LocalChange?
            var changeValue: Int = -1
            if (remove) {
                updateType = LocalChange.Update
            } else if (updates.position() >= MAX_UPDATE_SIZE) {
                updateType = null
            } else {
                val delta = player.tile.delta(Tile(viewport.lastSeen[player.index]))
                if (delta == Delta.EMPTY) {
                    changeValue = -1
                    updateType = if (flag != 0) LocalChange.Update else null
                } else {
                    val runIndex = getRunIndex(delta)
                    if (runIndex != -1 && player.movement.runStep != Direction.NONE) {
                        changeValue = runIndex
                        updateType = LocalChange.Run
                    } else {
                        val walkIndex = getWalkIndex(delta)
                        if (walkIndex != -1 && player.movement.walkStep != Direction.NONE) {
                            changeValue = walkIndex
                            updateType = LocalChange.Walk
                        } else if (withinView(delta)) {
                            changeValue = (delta.y and 0x1f) or (delta.x and 0x1f shl 5) or (delta.plane and 0x3 shl 10)
                            updateType = LocalChange.Tele
                        } else {
                            changeValue = (delta.y and 0x3fff) + (delta.x and 0x3fff shl 14) + (delta.plane and 0x3 shl 28)
                            updateType = LocalChange.TeleGlobal
                        }
                    }
                }
            }

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
            update = flag != 0 && !remove
            sync.writeBits(1, update)
            sync.writeBits(2, updateType.id)

            if (remove) {
                set.localPlayers[index] = false
                encodeRegion(sync, viewport, player)
                continue
            }

            when (updateType) {
                LocalChange.Walk, LocalChange.Run -> {
                    sync.writeBits(updateType.id + 2, changeValue)
                    viewport.lastSeen[player.index] = player.tile.id
                }
                LocalChange.Tele, LocalChange.TeleGlobal -> {
                    val global = updateType == LocalChange.TeleGlobal
                    sync.writeBits(1, global)
                    val size = if (global) 30 else 12
                    sync.writeBits(size, changeValue)
                    viewport.lastSeen[player.index] = player.tile.id
                }
                else -> {
                }
            }
            if (update) {
                val appearance = !(player.visuals.appearance.hashCode() == set.appearanceHash[index] || updates.position() + AppearanceEncoder.size(player.visuals.appearance) >= MAX_UPDATE_SIZE)
                if (appearance) {
                    set.appearanceHash[index] = player.visuals.appearance.hashCode()
                }
                encodeVisuals(updates, player.visuals, flag, encoders, !appearance)
            }
        }

        if (skip > -1) {
            writeSkip(sync, skip)
        }

        sync.finishBitAccess()
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
        set.addCount = 0
        for (i in 0 until set.outPlayersIndexesCount) {
            index = set.outPlayersIndexes[i]

            if (viewport.isActive(index) == active) {
                continue
            }

            player = players.indexed(index)

            viewport.setIdle(index)

            if (player == null) {
                skip++
                continue
            }

            val add = player.tile.within(client.tile, ViewportUpdating.VIEW_RADIUS) &&
                updates.position() < MAX_UPDATE_SIZE &&
                sync.position() < MAX_SYNC_SIZE
            if (!add) {
                skip++
                continue
            }

            if (skip > -1) {
                writeSkip(sync, skip)
                skip = -1
            }
            set.localPlayers[index] = true
            sync.writeBits(1, true)
            sync.writeBits(2, 0)
            encodeRegion(sync, viewport, player)
            sync.writeBits(6, player.tile.x and 0x3f)
            sync.writeBits(6, player.tile.y and 0x3f)
            viewport.lastSeen[player.index] = player.tile.id
            val appearance = player.visuals.appearance.hashCode() != set.appearanceHash[index]
            sync.writeBits(1, appearance)
            if (appearance) {
                set.appearanceHash[index] = player.visuals.appearance.hashCode()
                writeFlag(updates, initialFlag)
                for (encoder in initialEncoders) {
                    encoder.encode(updates, player.visuals)
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

    fun encodeRegion(sync: Writer, viewport: Viewport, player: Player): Boolean {
        val delta = player.tile.regionPlane.delta(Tile(viewport.lastSeen[player.index]).regionPlane)
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

    private fun encodeVisuals(updates: Writer, visuals: PlayerVisuals, flag: Int, encoders: List<VisualEncoder<PlayerVisuals>>, skipAppearance: Boolean) {
        if (flag == 0) {
            return
        }
        writeFlag(updates, flag)
        for (encoder in encoders) {
            if (!visuals.flagged(encoder.mask)) {
                continue
            }
            if (encoder::class == AppearanceEncoder::class && skipAppearance) {
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

    companion object {
        private const val MAX_PACKET_SIZE = 7500
        private const val MAX_SYNC_SIZE = 2500
        private const val MAX_UPDATE_SIZE = MAX_PACKET_SIZE - MAX_SYNC_SIZE - 100
    }
}