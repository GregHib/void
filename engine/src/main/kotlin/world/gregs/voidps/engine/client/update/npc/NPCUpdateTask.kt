package world.gregs.voidps.engine.client.update.npc

import it.unimi.dsi.fastutil.ints.IntSet
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.update.view.Viewport.Companion.LOCAL_NPC_CAP
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.region.RegionPlane
import world.gregs.voidps.network.encode.updateNPCs
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder

class NPCUpdateTask(
    private val npcs: NPCs,
    private val encoders: List<VisualEncoder<NPCVisuals>>
) {

    private val initialEncoders = encoders.filter { it.initial }
    private val initialFlag = initialEncoders.sumOf { it.mask }

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
            if (change == LocalChange.Remove || npc == null) {
                iterator.remove()
                continue
            }

            encodeMovement(change, sync, npc)
            encodeVisuals(updates, npc.visuals.flag, npc.visuals, encoders, client.index)
        }
    }

    /**
     * Calculate the type of update required for a local [npc]
     */
    private fun localChange(client: Player, viewport: Viewport, npc: NPC?): LocalChange {
        if (npc == null || !npc.tile.within(client.tile, viewport.radius)) {
            return LocalChange.Remove
        }
        val delta = npc.movement.delta
        if (delta == Delta.EMPTY) {
            return if (npc.visuals.flag != 0) LocalChange.Update else LocalChange.None
        }

        val movement = npc.movement
        if (movement.walkStep != Direction.NONE && npc.def["crawl", false]) {
            return LocalChange.Crawl
        }

        if (movement.runStep != Direction.NONE) {
            return LocalChange.Run
        }

        if (movement.walkStep != Direction.NONE) {
            return LocalChange.Walk
        }

        return LocalChange.Tele
    }

    private fun encodeMovement(change: LocalChange, sync: Writer, npc: NPC) {
        if (change is LocalChange.Movement) {
            if (change != LocalChange.Walk) {
                sync.writeBits(1, change == LocalChange.Run)
            }
            sync.writeBits(3, clockwise(npc.movement.walkStep))
            if (change == LocalChange.Run) {
                sync.writeBits(3, clockwise(npc.movement.runStep))
            }
            sync.writeBits(1, npc.visuals.flag != 0)
        }
    }

    private fun clockwise(step: Direction) = when (step) {
        Direction.NORTH -> 0
        Direction.NORTH_EAST -> 1
        Direction.EAST -> 2
        Direction.SOUTH_EAST -> 3
        Direction.SOUTH -> 4
        Direction.SOUTH_WEST -> 5
        Direction.WEST -> 6
        Direction.NORTH_WEST -> 7
        else -> -1
    }

    fun processAdditions(
        client: Player,
        viewport: Viewport,
        sync: Writer,
        updates: Writer,
        set: IntSet
    ) {
        var region: RegionPlane
        var npc: NPC
        for (direction in Direction.reversed) {
            region = client.tile.regionPlane.add(direction)
            for (index in npcs.getDirect(region) ?: continue) {
                npc = npcs.indexed(index) ?: continue
                if (!add(updates, sync, npc, client, viewport, set, index)) {
                    continue
                }
                val visuals = npc.visuals
                val flag = visuals.flag and initialFlag
                val delta = npc.tile.delta(client.tile)
                val teleporting = npc.movement.delta != Delta.EMPTY && npc.movement.walkStep == Direction.NONE && npc.movement.runStep == Direction.NONE
                set.add(npc.index)
                sync.writeBits(15, index)
                sync.writeBits(2, npc.tile.plane)
                sync.writeBits(1, teleporting)
                sync.writeBits(5, delta.y + if (delta.y < 15) 32 else 0)
                sync.writeBits(5, delta.x + if (delta.x < 15) 32 else 0)
                sync.writeBits(3, (visuals.turn.direction shr 11) - 4)
                sync.writeBits(1, flag != 0)
                sync.writeBits(14, npc.def.id)
                encodeVisuals(updates, flag, visuals, initialEncoders, client.index)
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

        return set.size < LOCAL_NPC_CAP && !set.contains(index) && npc.tile.within(client.tile, viewport.radius)
    }

    private fun encodeVisuals(updates: Writer, flag: Int, visuals: NPCVisuals, encoders: List<VisualEncoder<NPCVisuals>>, index: Int) {
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
        var flag = dataFlag

        if (flag >= 0x100) {
            flag = flag or 0x10
        }
        writer.writeByte(flag)

        if (flag >= 0x100) {
            writer.writeByte(flag shr 8)
        }
    }

    private sealed class LocalChange(val id: Int) {
        object None : LocalChange(-1)
        object Update : LocalChange(0)
        sealed class Movement(id: Int) : LocalChange(id)
        object Walk : Movement(1)
        object Crawl : Movement(2)
        object Run : Movement(2)
        object Tele : LocalChange(3)
        object Remove : LocalChange(3)
    }

    companion object {
        private const val MAX_PACKET_SIZE = 7500
        private const val MAX_SYNC_SIZE = 2500
        private const val MAX_UPDATE_SIZE = MAX_PACKET_SIZE - MAX_SYNC_SIZE - 100
    }
}