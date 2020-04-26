package rs.dusk.engine.entity.model.visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Visuals(
    var flag: Int = 0,
    var aspects: MutableMap<Int, Visual> = mutableMapOf(),
    var encoded: MutableMap<Int, ByteArray> = mutableMapOf(),
    var update: ByteArray? = null
) {

    inline fun <reified T : Visual> getOrPut(mask: Int, put: () -> T): T {
        return aspects.getOrPut(mask, put) as T
    }

    fun flag(mask: Int) {
        flag = flag or mask
    }

    fun flagged(mask: Int): Boolean {
        return flag and mask != 0
    }

    fun clear() {
        flag = 0
        aspects.clear()
        encoded.clear()
        update = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Visuals

        if (flag != other.flag) return false
        if (aspects != other.aspects) return false
        if (encoded != other.encoded) return false
        if (update != null) {
            if (other.update == null) return false
            if (!update!!.contentEquals(other.update!!)) return false
        } else if (other.update != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flag
        result = 31 * result + aspects.hashCode()
        result = 31 * result + encoded.hashCode()
        result = 31 * result + (update?.contentHashCode() ?: 0)
        return result
    }
}