package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_TIME_BAR_MASK
import world.gregs.voidps.engine.entity.character.update.visual.TimeBar
import world.gregs.voidps.utility.func.toInt

class PlayerTimeBarEncoder : VisualEncoder<TimeBar>(PLAYER_TIME_BAR_MASK) {

    override fun encode(writer: Writer, visual: TimeBar) {
        val (full, exponentialDelay, delay, increment) = visual
        writer.apply {
            writeShortLittle((full.toInt() * 0x8000) or (exponentialDelay and 0x7fff))
            writeByteSubtract(delay)
            writeByteInverse(increment)
        }
    }

}