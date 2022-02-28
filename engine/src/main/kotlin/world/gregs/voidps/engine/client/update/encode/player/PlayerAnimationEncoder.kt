package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.PlayerVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.Animation
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_ANIMATION_MASK

class PlayerAnimationEncoder : VisualEncoder<PlayerVisuals>(PLAYER_ANIMATION_MASK) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        val visual = visuals.aspects[mask] as Animation
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