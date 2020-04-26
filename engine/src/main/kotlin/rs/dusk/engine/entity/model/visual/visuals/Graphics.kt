package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Graphic
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Graphics(val graphics: Array<Graphic?> = arrayOfNulls(4)) : Visual {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Graphics

        if (!graphics.contentEquals(other.graphics)) return false

        return true
    }

    override fun hashCode(): Int {
        return graphics.contentHashCode()
    }
}