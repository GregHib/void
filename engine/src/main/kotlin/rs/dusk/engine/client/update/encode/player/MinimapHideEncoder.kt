package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.player.MinimapHide

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class MinimapHideEncoder : VisualEncoder<MinimapHide>(MinimapHide::class) {

    override fun encode(writer: Writer, visual: MinimapHide) {
        writer.writeByte(visual.hidden, Modifier.SUBTRACT)
    }

}