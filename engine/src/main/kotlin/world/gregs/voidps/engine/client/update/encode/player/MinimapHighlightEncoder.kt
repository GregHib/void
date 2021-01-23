package world.gregs.voidps.engine.client.update.encode.player

import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.player.MINIMAP_HIGHLIGHT_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.MinimapHighlight

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class MinimapHighlightEncoder : VisualEncoder<MinimapHighlight>(MINIMAP_HIGHLIGHT_MASK) {

    override fun encode(writer: Writer, visual: MinimapHighlight) {
        writer.writeByte(visual.highlighted, Modifier.SUBTRACT)
    }

}