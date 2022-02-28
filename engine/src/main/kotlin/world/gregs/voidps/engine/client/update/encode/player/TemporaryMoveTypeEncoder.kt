package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.PlayerVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.player.TEMPORARY_MOVE_TYPE_MASK

class TemporaryMoveTypeEncoder : VisualEncoder<PlayerVisuals>(TEMPORARY_MOVE_TYPE_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        writer.writeByteSubtract(visuals.temporaryMoveType.type.id)
    }

}