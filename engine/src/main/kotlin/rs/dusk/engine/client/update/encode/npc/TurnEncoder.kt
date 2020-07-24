package rs.dusk.engine.client.update.encode.npc

import rs.dusk.core.io.Endian
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.model.entity.character.update.VisualEncoder
import rs.dusk.engine.model.entity.character.update.visual.npc.TURN_MASK
import rs.dusk.engine.model.entity.character.update.visual.npc.Turn

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