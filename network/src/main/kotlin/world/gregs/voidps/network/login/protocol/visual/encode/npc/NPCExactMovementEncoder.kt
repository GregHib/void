package world.gregs.voidps.network.login.protocol.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_EXACT_MOVEMENT_MASK

class NPCExactMovementEncoder : VisualEncoder<NPCVisuals>(NPC_EXACT_MOVEMENT_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val (tile1X, tile1Y, delay1, tile2X, tile2Y, delay2, direction) = visuals.exactMovement
        writer.apply {
            p1Alt3(tile1X)
            p1Alt3(tile1Y)
            p1Alt2(tile2X)
            p1Alt2(tile2Y)
            writeShort(delay1)
            p2Alt3(delay2)
            p1Alt3(direction / 2)
        }
    }

}