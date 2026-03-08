package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Parameterized

data class WeaponAnimationDefinition(
    override var id: Int = -1,
    val attackTypes: Map<Int, String> = emptyMap(),
    override var stringId: String = "",
    override var params: Map<Int, Any>? = null,
) : Definition,
    Parameterized {

    companion object {
        val EMPTY = WeaponAnimationDefinition()

        @Suppress("UNCHECKED_CAST")
        fun fromMap(stringId: String, map: Map<Int, Any>) = WeaponAnimationDefinition(
            attackTypes = (map as? Map<Int, String>) ?: EMPTY.attackTypes,
            stringId = stringId,
        )
    }
}
