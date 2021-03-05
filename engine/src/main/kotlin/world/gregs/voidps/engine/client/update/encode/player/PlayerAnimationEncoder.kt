package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.Animation
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_ANIMATION_MASK

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class PlayerAnimationEncoder : VisualEncoder<Animation>(PLAYER_ANIMATION_MASK) {

    override fun encode(writer: Writer, visual: Animation) {
        val (first, second, third, fourth, speed) = visual
        writer.apply {
            writeShortAdd(first)
            writeShortAdd(second)
            writeShortAdd(third)
            writeShortAdd(fourth)
            writeByteAdd(speed)
        }
    }
}