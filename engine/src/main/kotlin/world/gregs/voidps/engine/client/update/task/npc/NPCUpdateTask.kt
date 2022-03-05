package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.update.task.LocalChange
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCTrackingSet
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.teleporting
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Viewport.Companion.VIEW_RADIUS
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

    fun run(player: Player) {
        val viewport = player.viewport
        val npcs = viewport.npcs

        val writer = viewport.npcChanges
        val updates = viewport.npcUpdates

        writer.startBitAccess()
        processLocals(player, writer, updates, npcs)
        processAdditions(player, writer, updates, npcs)
        writer.finishBitAccess()

        player.client?.updateNPCs(writer, updates)
    }

    fun processLocals(
        client: Player,
        sync: Writer,
        updates: Writer,
        set: NPCTrackingSet
    ) {
        sync.writeBits(8, set.locals.size)
        var npc: NPC?
        var index: Int
        val iterator = set.locals.iterator()
        while (iterator.hasNext()) {
            index = iterator.nextInt()
            npc = npcs.indexed(index)
            val remove = npc == null || !npc.tile.within(client.tile, VIEW_RADIUS)
            val change = if (remove) LocalChange.Remove else {
                when {
                    npc!!.movement.delta != Delta.EMPTY && npc.movement.walkStep != Direction.NONE && npc.def["crawl", false] -> LocalChange.Crawl
                    npc.movement.delta != Delta.EMPTY && npc.movement.runStep != Direction.NONE -> LocalChange.Run
                    npc.movement.delta != Delta.EMPTY && npc.movement.walkStep != Direction.NONE -> LocalChange.Walk
                    npc.movement.delta != Delta.EMPTY -> LocalChange.Tele
                    npc.visuals.flag != 0 -> LocalChange.Update
                    else -> null
                }
            }

            if (change == null) {
                sync.writeBits(1, false)
                continue
            }

            sync.writeBits(1, true)
            sync.writeBits(2, change.id)

            if (remove) {
                iterator.remove()
                continue
            }

            when (change) {
                LocalChange.Walk -> {
                    sync.writeBits(3, clockwise(npc!!.movement.walkStep))
                    sync.writeBits(1, npc.visuals.flag != 0)
                }
                LocalChange.Crawl -> {
                    sync.writeBits(1, false)
                    sync.writeBits(3, clockwise(npc!!.movement.walkStep))
                    sync.writeBits(1, npc.visuals.flag != 0)
                }
                LocalChange.Run -> {
                    sync.writeBits(1, true)
                    sync.writeBits(3, clockwise(npc!!.movement.walkStep))
                    sync.writeBits(3, clockwise(npc.movement.runStep))
                    sync.writeBits(1, npc.visuals.flag != 0)
                }
                else -> {
                }
            }
            if (!remove) {
                encodeVisuals(updates, npc!!.visuals, npc.visuals.flag, encoders)
            }
        }
    }

    fun processAdditions(
        client: Player,
        sync: Writer,
        updates: Writer,
        set: NPCTrackingSet
    ) {
        var region: RegionPlane
        var npc: NPC
        for (direction in Direction.reversed) {
            region = client.tile.regionPlane.add(direction)
            for (index in npcs.getDirect(region) ?: continue) {
                npc = npcs.indexed(index) ?: continue
                if (!npc.tile.within(client.tile, VIEW_RADIUS) || set.locals.size == set.localMax || set.locals.contains(index)) {
                    continue
                }
                set.locals.add(npc.index)
                val delta = npc.tile.delta(client.tile)
                sync.writeBits(15, index)
                sync.writeBits(2, npc.tile.plane)
                sync.writeBits(1, npc.teleporting)
                sync.writeBits(5, delta.y + if (delta.y < 15) 32 else 0)
                sync.writeBits(5, delta.x + if (delta.x < 15) 32 else 0)
                sync.writeBits(3, (npc.visuals.turn.direction shr 11) - 4)
                val flag = initialEncoders.filter { npc.visuals.flagged(it.mask) }.sumOf { it.mask }
                sync.writeBits(1, flag != 0)
                sync.writeBits(14, npc.def.id)
                encodeVisuals(updates, npc.visuals, flag, initialEncoders)
            }
        }
        sync.writeBits(15, -1)
    }

    private fun encodeVisuals(updates: Writer, visuals: NPCVisuals, flag: Int, encoders: List<VisualEncoder<NPCVisuals>>) {
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
            flag = flag or 0x10
        }
        writer.writeByte(flag)

        if (flag >= 0x100) {
            writer.writeByte(flag shr 8)
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

}