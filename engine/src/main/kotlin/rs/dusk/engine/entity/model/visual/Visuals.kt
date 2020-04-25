package rs.dusk.engine.entity.model.visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Visuals(
    var flag: Int = 0,
    var aspects: MutableMap<Int, Visual> = mutableMapOf(),
    var encoded: ByteArray? = null
) {

    fun add(flag: Int, visual: Visual) {
        this.flag = this.flag or flag
        aspects[flag] = visual
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Visuals

        if (flag != other.flag) return false
        if (aspects != other.aspects) return false
        if (encoded != null) {
            if (other.encoded == null) return false
            if (!encoded!!.contentEquals(other.encoded!!)) return false
        } else if (other.encoded != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flag
        result = 31 * result + aspects.hashCode()
        result = 31 * result + (encoded?.contentHashCode() ?: 0)
        return result
    }
}