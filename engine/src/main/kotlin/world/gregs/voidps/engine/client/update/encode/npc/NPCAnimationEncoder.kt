package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.NPCVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.NPC_ANIMATION_MASK

class NPCAnimationEncoder : VisualEncoder<NPCVisuals>(NPC_ANIMATION_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val (first, second, third, fourth, speed) = visuals.animation
        writer.apply {
            writeShort(first)
            writeShort(second)
            writeShort(third)
            writeShort(fourth)
            writeByteSubtract(speed)
        }
    }
}