package world.gregs.void.engine.client.update.encode.player

import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.write.Writer
import world.gregs.void.engine.entity.character.update.VisualEncoder
import world.gregs.void.engine.entity.character.update.visual.player.MINIMAP_HIGHLIGHT_MASK
import world.gregs.void.engine.entity.character.update.visual.player.MinimapHighlight

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class MinimapHighlightEncoder : VisualEncoder<MinimapHighlight>(MINIMAP_HIGHLIGHT_MASK) {

    override fun encode(writer: Writer, visual: MinimapHighlight) {
        writer.writeByte(visual.highlighted, Modifier.SUBTRACT)
    }

}