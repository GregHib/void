package rs.dusk.cache.definition.data

import rs.dusk.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since December 12, 2020
 */
data class ClientScriptDefinition(
    override var id: Int = -1,
    var intArgumentCount: Int = 0,
    var stringVariableCount: Int = 0,
    var longVariableCount: Int = 0,
    var intVariableCount: Int = 0,
    var stringArgumentCount: Int = 0,
    var longArgumentCount: Int = 0,
    var aHashTableArray9503: Array<List<Pair<Int, Int>>>? = null,
    var name: String? = null,
    var instructions: IntArray = intArrayOf(),
    var stringOperands: Array<String?>? = null,
    var longOperands: LongArray? = null,
    var intOperands: IntArray? = null
) : Definition {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientScriptDefinition

        if (id != other.id) return false
        if (intArgumentCount != other.intArgumentCount) return false
        if (stringVariableCount != other.stringVariableCount) return false
        if (longVariableCount != other.longVariableCount) return false
        if (intVariableCount != other.intVariableCount) return false
        if (stringArgumentCount != other.stringArgumentCount) return false
        if (longArgumentCount != other.longArgumentCount) return false
        if (aHashTableArray9503 != null) {
            if (other.aHashTableArray9503 == null) return false
            if (!aHashTableArray9503!!.contentEquals(other.aHashTableArray9503!!)) return false
        } else if (other.aHashTableArray9503 != null) return false
        if (name != other.name) return false
        if (!instructions.contentEquals(other.instructions)) return false
        if (stringOperands != null) {
            if (other.stringOperands == null) return false
            if (!stringOperands!!.contentEquals(other.stringOperands!!)) return false
        } else if (other.stringOperands != null) return false
        if (longOperands != null) {
            if (other.longOperands == null) return false
            if (!longOperands!!.contentEquals(other.longOperands!!)) return false
        } else if (other.longOperands != null) return false
        if (intOperands != null) {
            if (other.intOperands == null) return false
            if (!intOperands!!.contentEquals(other.intOperands!!)) return false
        } else if (other.intOperands != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + intArgumentCount
        result = 31 * result + stringVariableCount
        result = 31 * result + longVariableCount
        result = 31 * result + intVariableCount
        result = 31 * result + stringArgumentCount
        result = 31 * result + longArgumentCount
        result = 31 * result + (aHashTableArray9503?.contentHashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + instructions.contentHashCode()
        result = 31 * result + (stringOperands?.contentHashCode() ?: 0)
        result = 31 * result + (longOperands?.contentHashCode() ?: 0)
        result = 31 * result + (intOperands?.contentHashCode() ?: 0)
        return result
    }
}