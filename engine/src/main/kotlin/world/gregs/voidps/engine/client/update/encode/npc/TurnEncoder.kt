package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.npc.TURN_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.Turn

class TurnEncoder : VisualEncoder<Turn>(TURN_MASK) {

    override fun encode(writer: Writer, visual: Turn) {
        val (x, y, directionX, directionY) = visual
        writer.apply {
            writeShortAdd((x + directionX) * 2 + 1)
            writeShortLittle((y + directionY) * 2 + 1)
        }
    }

}