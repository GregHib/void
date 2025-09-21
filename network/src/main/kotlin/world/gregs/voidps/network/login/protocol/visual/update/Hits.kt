package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual

data class Hits(
    val splats: Array<HitSplat?> = arrayOfNulls(6),
    var self: Int = 0,
    var target: Int = 0,
) : Visual {
    fun add(hit: HitSplat) {
        for (i in splats.indices) {
            if (splats[i] == null) {
                splats[i] = hit
                break
            }
        }
    }

    override fun needsReset(): Boolean = splats.any { it != null }

    override fun reset() {
        splats.fill(null)
        target = 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Hits

        if (self != other.self) return false
        if (target != other.target) return false
        if (!splats.contentEquals(other.splats)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = self
        result = 31 * result + target
        result = 31 * result + splats.contentHashCode()
        return result
    }
}
