package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
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
            writeByte(tile1.x, Modifier.SUBTRACT)
            writeByte(tile1.y, Modifier.SUBTRACT)
            writeByte(tile2.x, Modifier.NONE)
            writeByte(tile2.y, Modifier.SUBTRACT)
            writeShort(delay1, Modifier.NONE, Endian.LITTLE)
            writeShort(delay2, Modifier.ADD)
            writeByte(direction.ordinal / 2, Modifier.SUBTRACT)
        }
    }

}