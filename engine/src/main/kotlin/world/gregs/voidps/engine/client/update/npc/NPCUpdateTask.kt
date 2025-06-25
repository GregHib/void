package world.gregs.voidps.engine.client.update.npc

import it.unimi.dsi.fastutil.ints.IntSet
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.update.view.Viewport.Companion.LOCAL_NPC_CAP
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.login.protocol.encode.updateNPCs
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.RegionLevel

class NPCUpdateTask(
    private val npcs: NPCs,
    private val encoders: List<VisualEncoder<NPCVisuals>>
) {

    fun run(player: Player) {
        val viewport = player.viewport ?: return
        val npcs = viewport.npcs

        val writer = viewport.npcChanges
        val updates = viewport.npcUpdates

        writer.startBitAccess()
        processLocals(player, viewport, writer, updates, npcs)
        processAdditions(player, viewport, writer, updates, npcs)
        writer.stopBitAccess()

        player.client?.updateNPCs(writer, updates)
        player.client?.flush()
        writer.position(0)
        updates.position(0)
    }

    fun processLocals(
        client: Player,
        viewport: Viewport,
        sync: Writer,
        updates: Writer,
        set: IntSet
    ) {
        var index: Int
        var npc: NPC?
        val iterator = set.intIterator()
        sync.writeBits(8, set.size)
        while (iterator.hasNext()) {
            index = iterator.nextInt()
            npc = npcs.indexed(index)

            val change = localChange(client, viewport, npc)
            sync.writeBits(1, change != LocalChange.None)
            if (change == LocalChange.None) {
                continue
            }

            sync.writeBits(2, change.id)
            if (change == LocalChange.Remove || change == LocalChange.Tele || npc == null) {
                iterator.remove()
                continue
            }

            encodeMovement(change, sync, npc)
            encodeVisuals(updates, npc.visuals.flag, npc.visuals, client.index)
        }
    }

    /**
     * Calculate the type of update required for a local [npc]
     */
    private fun localChange(client: Player, viewport: Viewport, npc: NPC?): LocalChange {
        if (npc == null || npc.hide || !npc.tile.within(client.tile, viewport.radius)) {
            return LocalChange.Remove
        }
        val visuals = npc.visuals
        if (!visuals.moved) {
            return if (visuals.flag != 0) LocalChange.Update else LocalChange.None
        }

        if (visuals.walkStep != -1 && npc.def["crawl", false]) {
            return LocalChange.Crawl
        }

        if (visuals.runStep != -1) {
            return LocalChange.Run
        }

        if (visuals.walkStep != -1) {
            return LocalChange.Walk
        }

        return LocalChange.Tele
    }

    private fun encodeMovement(change: LocalChange, sync: Writer, npc: NPC) {
        if (change !is LocalChange.Move) {
            return
        }
        if (change != LocalChange.Walk) {
            sync.writeBits(1, change == LocalChange.Run)
        }
        sync.writeBits(3, npc.visuals.walkStep)
        if (change == LocalChange.Run) {
            sync.writeBits(3, npc.visuals.runStep)
        }
        sync.writeBits(1, npc.visuals.flag != 0)
    }

    fun processAdditions(
        client: Player,
        viewport: Viewport,
        sync: Writer,
        updates: Writer,
        set: IntSet
    ) {
        var region: RegionLevel
        var npc: NPC
        for (direction in Direction.reversed) {
            region = client.tile.regionLevel.add(direction)
            for (index in npcs.getDirect(region) ?: continue) {
                npc = npcs.indexed(index) ?: continue
                if (!add(updates, sync, npc, client, viewport, set, index)) {
                    continue
                }
                val visuals = npc.visuals
                val flag = visuals.flag
                val delta = npc.tile.delta(client.tile)
                val teleporting = visuals.moved && visuals.walkStep == -1 && visuals.runStep == -1
                set.add(npc.index)
                sync.writeBits(15, index)
                sync.writeBits(3, (visuals.face.direction shr 11) - 4)
                sync.writeBits(1, flag != 0)
                sync.writeBits(5, delta.y + if (delta.y < 15) 32 else 0)
                sync.writeBits(2, npc.tile.level)
                sync.writeBits(15, npc.def.id)
                sync.writeBits(5, delta.x + if (delta.x < 15) 32 else 0)
                sync.writeBits(1, teleporting)

                encodeVisuals(updates, flag, visuals, client.index)
            }
        }
        sync.writeBits(15, -1)
    }

    private fun add(updates: Writer, sync: Writer, npc: NPC, client: Player, viewport: Viewport, set: IntSet, index: Int): Boolean {
        if (sync.position() >= MAX_SYNC_SIZE) {
            return false
        }
        if (updates.position() >= MAX_UPDATE_SIZE) {
            return false
        }
        if (npc.hide) {
            return false
        }
        return set.size < LOCAL_NPC_CAP && !set.contains(index) && npc.tile.within(client.tile, viewport.radius)
    }

    private fun encodeVisuals(updates: Writer, flag: Int, visuals: NPCVisuals, index: Int) {
        if (flag == 0) {
            return
        }
        writeFlag(updates, flag)
        for (encoder in encoders) {
            if (flag and encoder.mask == 0) {
                continue
            }
            encoder.encode(updates, visuals, index)
        }
    }

    fun writeFlag(writer: Writer, dataFlag: Int) {
        var first = dataFlag and 0xFF
        if (dataFlag > 0x7F) {
            first = first or 0x80
        }
        writer.writeByte(first)

        if (dataFlag > 0x7F) {
            var second = (dataFlag shr 8) and 0xFF
            if (dataFlag > 0x7FFF) {
                second = second or 0x80
            }
            writer.writeByte(second)

            if (dataFlag > 0x7FFF) {
                val third = (dataFlag shr 16) and 0xFF
                writer.writeByte(third)
            }
        }
    }

    private sealed class LocalChange(val id: Int) {
        data object None : LocalChange(-1)
        data object Update : LocalChange(0)
        sealed class Move(id: Int) : LocalChange(id)
        data object Walk : Move(1)
        data object Crawl : Move(2)
        data object Run : Move(2)
        data object Tele : LocalChange(3)
        data object Remove : LocalChange(3)
    }

    companion object {
        private const val MAX_PACKET_SIZE = 7500
        private const val MAX_SYNC_SIZE = 2500
        private const val MAX_UPDATE_SIZE = MAX_PACKET_SIZE - MAX_SYNC_SIZE - 100
    }
}