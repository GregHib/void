package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCTrackingSet
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.teleporting
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.LocalChange
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

        processLocals(writer, updates, npcs)
        processAdditions(writer, updates, player, npcs)

        player.client?.updateNPCs(writer, updates)
    }

    fun processLocals(
        sync: Writer,
        updates: Writer,
        set: NPCTrackingSet
    ) {
        sync.startBitAccess()
        sync.writeBits(8, set.locals.size)
        var npc: NPC
        for (index in set.locals.intIterator()) {
            npc = npcs.indexed(index)!!
            val remove = set.remove(index)
            val change = if (remove) LocalChange.Remove else npc.change

            if (change == null) {
                sync.writeBits(1, false)
                continue
            }

            sync.writeBits(1, true)
            sync.writeBits(2, change.id)

            when (change) {
                LocalChange.Walk -> {
                    sync.writeBits(3, npc.walkDirection)
                    sync.writeBits(1, npc.visuals.flag != 0)
                }
                LocalChange.Crawl -> {
                    sync.writeBits(1, false)
                    sync.writeBits(3, npc.walkDirection)
                    sync.writeBits(1, npc.visuals.flag != 0)
                }
                LocalChange.Run -> {
                    sync.writeBits(1, true)
                    sync.writeBits(3, npc.walkDirection)
                    sync.writeBits(3, npc.runDirection)
                    sync.writeBits(1, npc.visuals.flag != 0)
                }
                else -> {
                }
            }
            if (!remove) {
                encodeVisuals(updates, npc.visuals, npc.visuals.flag, encoders)
            }
        }

        sync.finishBitAccess()
    }

    fun processAdditions(
        sync: Writer,
        updates: Writer,
        client: Player,
        set: NPCTrackingSet
    ) {
        var npc: NPC
        var index: Int
        for (i in 0 until set.addCount) {
            index = set.add[i]
            npc = npcs.indexed(index)!!
            val delta = npc.tile.delta(client.tile)
            sync.writeBits(15, npc.index)
            sync.writeBits(2, npc.tile.plane)
            sync.writeBits(1, npc.teleporting)
            sync.writeBits(5, delta.y + if (delta.y < 15) 32 else 0)
            sync.writeBits(5, delta.x + if (delta.x < 15) 32 else 0)
            sync.writeBits(3, (npc.visuals.turn.direction shr 11) - 4)
            val visuals = npc.visuals
            val flag = initialEncoders.filter { visuals.flagged(it.mask) }.sumOf { it.mask }
            sync.writeBits(1, flag != 0)
            sync.writeBits(14, npc.def.id)
            encodeVisuals(updates, npc.visuals, flag, initialEncoders)
        }
        sync.writeBits(15, -1)
        sync.finishBitAccess()
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

}