package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_EXACT_MOVEMENT_MASK

class NPCExactMovementEncoder : VisualEncoder<NPCVisuals>(NPC_EXACT_MOVEMENT_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val (tile1X, tile1Y, delay1, tile2X, tile2Y, delay2, direction) = visuals.exactMovement
        writer.apply {
            writeByteSubtract(tile1X)
            writeByteSubtract(tile1Y)
            writeByteInverse(tile2X)
            writeByteInverse(tile2Y)
            writeShort(delay1)
            writeShortAddLittle(delay2)
            writeByteSubtract(direction / 2)
        }
    }

}