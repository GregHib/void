package rs.dusk.engine.client.update.encode.player

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.visuals.player.Appearance

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class AppearanceEncoder : VisualEncoder<Appearance>(Appearance::class) {

    override fun encode(writer: Writer, visual: Appearance) {
        writer.apply {
            writeByte(visual.data.size, Modifier.SUBTRACT)
            writeBytes(visual.data)
        }
    }

}