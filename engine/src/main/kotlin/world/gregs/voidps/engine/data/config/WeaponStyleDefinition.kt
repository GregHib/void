package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class WeaponStyleDefinition(
    override var id: Int = -1,
    val attackTypes: Array<String> = emptyArray(),
    val attackStyles: Array<String> = emptyArray(),
    val combatStyles: Array<String> = emptyArray(),
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Extra {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WeaponStyleDefinition

        if (id != other.id) return false
        if (!attackTypes.contentEquals(other.attackTypes)) return false
        if (!attackStyles.contentEquals(other.attackStyles)) return false
        if (!combatStyles.contentEquals(other.combatStyles)) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + attackTypes.contentHashCode()
        result = 31 * result + attackStyles.contentHashCode()
        result = 31 * result + combatStyles.contentHashCode()
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        val EMPTY = WeaponStyleDefinition()

        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: Int, stringId: String, map: Map<String, Any>) = WeaponStyleDefinition(
            id = id,
            attackTypes = (map["attack_types"] as? List<String>)?.toTypedArray() ?: EMPTY.attackTypes,
            attackStyles = (map["attack_styles"] as? List<String>)?.toTypedArray() ?: EMPTY.attackTypes,
            combatStyles = (map["combat_styles"] as? List<String>)?.toTypedArray() ?: EMPTY.attackTypes,
            stringId = stringId,
        )
    }
}
