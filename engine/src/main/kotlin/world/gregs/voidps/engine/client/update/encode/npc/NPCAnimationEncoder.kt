package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.character.update.visual.Animation
import world.gregs.voidps.engine.entity.character.update.visual.NPC_ANIMATION_MASK

class NPCAnimationEncoder : VisualEncoder(NPC_ANIMATION_MASK) {

    override fun encode(writer: Writer, visuals: Visuals) {
        val visual = visuals.aspects[mask] as Animation
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