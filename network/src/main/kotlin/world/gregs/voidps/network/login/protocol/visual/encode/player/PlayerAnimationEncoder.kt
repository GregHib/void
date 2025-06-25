package world.gregs.voidps.network.login.protocol.visual.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_ANIMATION_MASK

class PlayerAnimationEncoder : VisualEncoder<PlayerVisuals>(PLAYER_ANIMATION_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val (first, second, third, fourth, delay) = visuals.animation
        val animations = arrayOf(first, second, third, fourth)
        writer.apply {
            for (anim in animations) {
                ip2(anim)
            }
            writeByteAdd(delay)
        }
    }
}