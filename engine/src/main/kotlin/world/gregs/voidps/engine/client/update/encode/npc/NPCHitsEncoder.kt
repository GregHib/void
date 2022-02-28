package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.NPCVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.NPC_HITS_MASK

class NPCHitsEncoder : VisualEncoder<NPCVisuals>(NPC_HITS_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val (damage, player, other) = visuals.hits
        writer.apply {
            writeByteSubtract(damage.size)
            damage.forEach { hit ->
                hit.write(writer, player, other, add = false)
            }
        }
    }

}