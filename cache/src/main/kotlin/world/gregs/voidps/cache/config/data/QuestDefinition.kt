package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class QuestDefinition(
    override var id: Int = -1,
    var aString2211: String? = null,
    var aString2202: String? = null,
    var anIntArrayArray2208: Array<IntArray>? = null,
    var anIntArrayArray2193: Array<IntArray>? = null,
    var anIntArray2209: IntArray? = null,
    var anIntArray2207: IntArray? = null,
    var anIntArrayArray2210: Array<IntArray>? = null,
    var anInt2188: Int = -1,
    var anIntArray2200: IntArray? = null,
    var anIntArray2191: IntArray? = null,
    var anIntArray2199: IntArray? = null,
    var aStringArray2201: Array<String?>? = null,
    var anIntArray2204: IntArray? = null,
    var anIntArray2195: IntArray? = null,
    var anIntArray2190: IntArray? = null,
    var aStringArray2198: Array<String?>? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition, Extra {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuestDefinition

        if (id != other.id) return false
        if (aString2211 != other.aString2211) return false
        if (aString2202 != other.aString2202) return false
        if (anIntArrayArray2208 != null) {
            if (other.anIntArrayArray2208 == null) return false
            if (!anIntArrayArray2208.contentDeepEquals(other.anIntArrayArray2208)) return false
        } else if (other.anIntArrayArray2208 != null) return false
        if (anIntArrayArray2193 != null) {
            if (other.anIntArrayArray2193 == null) return false
            if (!anIntArrayArray2193.contentDeepEquals(other.anIntArrayArray2193)) return false
        } else if (other.anIntArrayArray2193 != null) return false
        if (anIntArray2209 != null) {
            if (other.anIntArray2209 == null) return false
            if (!anIntArray2209.contentEquals(other.anIntArray2209)) return false
        } else if (other.anIntArray2209 != null) return false
        if (anIntArray2207 != null) {
            if (other.anIntArray2207 == null) return false
            if (!anIntArray2207.contentEquals(other.anIntArray2207)) return false
        } else if (other.anIntArray2207 != null) return false
        if (anIntArrayArray2210 != null) {
            if (other.anIntArrayArray2210 == null) return false
            if (!anIntArrayArray2210.contentDeepEquals(other.anIntArrayArray2210)) return false
        } else if (other.anIntArrayArray2210 != null) return false
        if (anInt2188 != other.anInt2188) return false
        if (anIntArray2200 != null) {
            if (other.anIntArray2200 == null) return false
            if (!anIntArray2200.contentEquals(other.anIntArray2200)) return false
        } else if (other.anIntArray2200 != null) return false
        if (anIntArray2191 != null) {
            if (other.anIntArray2191 == null) return false
            if (!anIntArray2191.contentEquals(other.anIntArray2191)) return false
        } else if (other.anIntArray2191 != null) return false
        if (anIntArray2199 != null) {
            if (other.anIntArray2199 == null) return false
            if (!anIntArray2199.contentEquals(other.anIntArray2199)) return false
        } else if (other.anIntArray2199 != null) return false
        if (aStringArray2201 != null) {
            if (other.aStringArray2201 == null) return false
            if (!aStringArray2201.contentEquals(other.aStringArray2201)) return false
        } else if (other.aStringArray2201 != null) return false
        if (anIntArray2204 != null) {
            if (other.anIntArray2204 == null) return false
            if (!anIntArray2204.contentEquals(other.anIntArray2204)) return false
        } else if (other.anIntArray2204 != null) return false
        if (anIntArray2195 != null) {
            if (other.anIntArray2195 == null) return false
            if (!anIntArray2195.contentEquals(other.anIntArray2195)) return false
        } else if (other.anIntArray2195 != null) return false
        if (anIntArray2190 != null) {
            if (other.anIntArray2190 == null) return false
            if (!anIntArray2190.contentEquals(other.anIntArray2190)) return false
        } else if (other.anIntArray2190 != null) return false
        if (aStringArray2198 != null) {
            if (other.aStringArray2198 == null) return false
            if (!aStringArray2198.contentEquals(other.aStringArray2198)) return false
        } else if (other.aStringArray2198 != null) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (aString2211?.hashCode() ?: 0)
        result = 31 * result + (aString2202?.hashCode() ?: 0)
        result = 31 * result + (anIntArrayArray2208?.contentDeepHashCode() ?: 0)
        result = 31 * result + (anIntArrayArray2193?.contentDeepHashCode() ?: 0)
        result = 31 * result + (anIntArray2209?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray2207?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArrayArray2210?.contentDeepHashCode() ?: 0)
        result = 31 * result + anInt2188
        result = 31 * result + (anIntArray2200?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray2191?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray2199?.contentHashCode() ?: 0)
        result = 31 * result + (aStringArray2201?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray2204?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray2195?.contentHashCode() ?: 0)
        result = 31 * result + (anIntArray2190?.contentHashCode() ?: 0)
        result = 31 * result + (aStringArray2198?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        val EMPTY = QuestDefinition()
    }
}