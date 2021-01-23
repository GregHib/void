package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.npc.TURN_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.Turn

/**
 * @author GregHib <greg@gregs.world>
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