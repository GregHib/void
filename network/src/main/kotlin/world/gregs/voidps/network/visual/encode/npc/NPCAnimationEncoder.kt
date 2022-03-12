package world.gregs.voidps.network.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.NPC_ANIMATION_MASK

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