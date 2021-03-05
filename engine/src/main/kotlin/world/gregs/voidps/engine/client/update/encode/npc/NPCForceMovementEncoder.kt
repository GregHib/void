package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.ForceMovement
import world.gregs.voidps.engine.entity.character.update.visual.NPC_FORCE_MOVEMENT_MASK

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class NPCForceMovementEncoder : VisualEncoder<ForceMovement>(NPC_FORCE_MOVEMENT_MASK) {

    override fun encode(writer: Writer, visual: ForceMovement) {
        val (tile1, delay1, tile2, delay2, direction) = visual
        writer.apply {
            writeByteSubtract(tile1.x)
            writeByteSubtract(tile1.y)
            writeByte(tile2.x)
            writeByteSubtract(tile2.y)
            writeShortLittle(delay1)
            writeShortAdd(delay2)
            writeByteSubtract(direction.ordinal / 2)
        }
    }

}