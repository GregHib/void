package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.NPCVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.npc.TURN_MASK

class TurnEncoder : VisualEncoder<NPCVisuals>(TURN_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val (x, y, directionX, directionY) = visuals.turn
        writer.apply {
            writeShortAdd((x + directionX) * 2 + 1)
            writeShortLittle((y + directionY) * 2 + 1)
        }
    }

}