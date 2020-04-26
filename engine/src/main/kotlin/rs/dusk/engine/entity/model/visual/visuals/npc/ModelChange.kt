package rs.dusk.engine.entity.model.visual.visuals.npc

import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ModelChange(var models: IntArray? = null, var colours: IntArray? = null, var textures: IntArray? = null) :
    Visual {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelChange

        if (models != null) {
            if (other.models == null) return false
            if (!models!!.contentEquals(other.models!!)) return false
        } else if (other.models != null) return false
        if (colours != null) {
            if (other.colours == null) return false
            if (!colours!!.contentEquals(other.colours!!)) return false
        } else if (other.colours != null) return false
        if (textures != null) {
            if (other.textures == null) return false
            if (!textures!!.contentEquals(other.textures!!)) return false
        } else if (other.textures != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = models?.contentHashCode() ?: 0
        result = 31 * result + (colours?.contentHashCode() ?: 0)
        result = 31 * result + (textures?.contentHashCode() ?: 0)
        return result
    }

}