package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.model.entity.character.update.VisualEncoder
import rs.dusk.engine.model.entity.character.update.visual.player.MINIMAP_HIGHLIGHT_MASK
import rs.dusk.engine.model.entity.character.update.visual.player.MinimapHighlight

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class MinimapHighlightEncoder : VisualEncoder<MinimapHighlight>(MINIMAP_HIGHLIGHT_MASK) {

    override fun encode(writer: Writer, visual: MinimapHighlight) {
        writer.writeByte(visual.highlighted, Modifier.SUBTRACT)
    }

}