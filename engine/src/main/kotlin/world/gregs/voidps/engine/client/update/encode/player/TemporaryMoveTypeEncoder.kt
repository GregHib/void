package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.character.update.visual.player.TEMPORARY_MOVE_TYPE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.TemporaryMoveType

class TemporaryMoveTypeEncoder : VisualEncoder(TEMPORARY_MOVE_TYPE_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: Visuals) {
        val visual = visuals.aspects[mask] as TemporaryMoveType
        writer.writeByteSubtract(visual.type.id)
    }

}