package world.gregs.void.engine.client.update.encode.npc

import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.write.Writer
import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.npc.TURN_MASK
import world.gregs.void.engine.entity.character.update.visual.npc.Turn

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class TurnEncoder : VisualEncoder<Turn>(TURN_MASK) {

    override fun encode(writer: Writer, visual: Turn) {
        val (x, y, directionX, directionY) = visual
        writer.apply {
            writeShort((x + directionX) * 2 + 1, order = Endian.LITTLE)
            writeShort((y + directionY) * 2 + 1, order = Endian.LITTLE)
        }
    }

}