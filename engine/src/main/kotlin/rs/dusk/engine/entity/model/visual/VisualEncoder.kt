package rs.dusk.engine.entity.model.visual

import rs.dusk.core.io.write.Writer

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
interface VisualEncoder<V : Visual> {
    fun encode(writer: Writer, visual: V)
}