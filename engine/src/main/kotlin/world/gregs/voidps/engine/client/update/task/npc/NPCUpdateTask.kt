package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCTrackingSet
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.teleporting
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.visual.npc.getTurn
import world.gregs.voidps.network.encode.updateNPCs

class NPCUpdateTask(
    private val npcs: NPCs
) {

    fun run(player: Player) {
        val viewport = player.viewport
        val npcs = viewport.npcs

        val writer = viewport.npcChanges
        val updates = viewport.npcUpdates

        processLocals(writer, updates, npcs)
        processAdditions(writer, updates, player, npcs)

        if (player.client == null) {
            writer.clear()
            updates.clear()
        }
        player.client?.updateNPCs(writer, updates)
    }

    fun processLocals(
        sync: Writer,
        updates: Writer,
        set: NPCTrackingSet
    ) {
        sync.startBitAccess()
        sync.writeBits(8, set.lastIndex)
        var npc: NPC
        var index: Int
        for (i in set.indices) {
            index = set.locals[i]
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
                    sync.writeBits(1, npc.visuals.update != null)
                }
                LocalChange.Crawl -> {
                    sync.writeBits(1, false)
                    sync.writeBits(3, npc.walkDirection)
                    sync.writeBits(1, npc.visuals.update != null)
                }
                LocalChange.Run -> {
                    sync.writeBits(1, true)
                    sync.writeBits(3, npc.walkDirection)
                    sync.writeBits(3, npc.runDirection)
                    sync.writeBits(1, npc.visuals.update != null)
                }
                else -> {
                }
            }
            if (!remove) {
                updates.writeBytes(npc.visuals.update ?: continue)
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
        for (i in set.addIndices) {
            index = set.add[i]
            npc = npcs.indexed(index)!!
            val (x, y) = npc.tile.delta(client.tile)
            sync.writeBits(15, npc.index)
            sync.writeBits(2, npc.tile.plane)
            sync.writeBits(1, npc.teleporting)
            sync.writeBits(5, y + if (y < 15) 32 else 0)
            sync.writeBits(5, x + if (x < 15) 32 else 0)
            sync.writeBits(3, (npc.getTurn().direction shr 11) - 4)
            sync.writeBits(1, npc.visuals.addition != null)
            sync.writeBits(14, npc.def.id)
            updates.writeBytes(npc.visuals.addition ?: continue)
        }
        sync.writeBits(15, -1)
        sync.finishBitAccess()
    }

}