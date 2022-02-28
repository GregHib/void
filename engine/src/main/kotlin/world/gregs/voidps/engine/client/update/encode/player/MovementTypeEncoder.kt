package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.PlayerVisuals
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.player.MOVEMENT_TYPE_MASK

class MovementTypeEncoder : VisualEncoder<PlayerVisuals>(MOVEMENT_TYPE_MASK, initial = true) {

    override fun encode(writer: Writer, visuals: PlayerVisuals) {
        writer.writeByteInverse(visuals.movementType.type.id)
    }

}