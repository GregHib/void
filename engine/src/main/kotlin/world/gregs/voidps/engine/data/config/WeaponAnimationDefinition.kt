package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.cache.definition.Params

data class WeaponAnimationDefinition(
    override var id: Int = -1,
    val attackTypes: Map<Int, String> = emptyMap(),
    override var stringId: String = "",
    override var extras: Map<Int, Any>? = null,
) : Definition,
    Extra {

    companion object {
        val EMPTY = WeaponAnimationDefinition()

        @Suppress("UNCHECKED_CAST")
        fun fromMap(stringId: String, map: Map<Int, Any>) = WeaponAnimationDefinition(
            attackTypes = (map as? Map<Int, String>) ?: EMPTY.attackTypes,
            stringId = stringId,
        )
    }
}
