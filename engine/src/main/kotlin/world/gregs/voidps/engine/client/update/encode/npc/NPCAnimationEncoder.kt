package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.Animation
import world.gregs.voidps.engine.entity.character.update.visual.NPC_ANIMATION_MASK

class NPCAnimationEncoder : VisualEncoder<Animation>(NPC_ANIMATION_MASK) {

    override fun encode(writer: Writer, visual: Animation) {
        val (first, second, third, fourth, speed) = visual
        writer.apply {
            writeShort(first)
            writeShort(second)
            writeShort(third)
            writeShort(fourth)
            writeByteSubtract(speed)
        }
    }
}