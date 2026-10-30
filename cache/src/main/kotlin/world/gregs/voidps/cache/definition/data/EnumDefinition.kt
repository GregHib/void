package world.gregs.voidps.cache.definition.data

import kotlinx.serialization.Serializable
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.serialization.EnumMapSerializer
import world.gregs.voidps.cache.serialization.ParamsSerializer
import world.gregs.voidps.type.random

@Serializable
data class EnumDefinition(
    override var id: Int = -1,
    var keyType: Char = 0.toChar(),
    var valueType: Char = 0.toChar(),
    var defaultString: String = "null",
    var defaultInt: Int = 0,
    var length: Int = 0,
    @Serializable(with = EnumMapSerializer::class)
    var map: Map<Int, Any>? = null,
    override var stringId: String = "",
    @Serializable(with = ParamsSerializer::class)
    override var params: Map<Int, Any>? = null,
) : Definition,
    Parameterized {
    fun getKey(value: Any) = map?.filterValues { it == value }?.keys?.lastOrNull() ?: -1

    fun int(id: Int) = map?.get(id) as? Int ?: defaultInt

    fun intOrNull(id: Int) = map?.get(id) as? Int

    fun randomInt() = map?.values?.random(random) as? Int ?: defaultInt

    fun string(id: Int) = map?.get(id) as? String ?: defaultString

    fun stringOrNull(id: Int) = map?.get(id) as? String

    override fun toString(): String = "EnumDefinition(id=$id, keyType=${EnumTypes.name(keyType)}, valueType=${EnumTypes.name(valueType)}, defaultString=$defaultString, defaultInt=$defaultInt, length=$length, map=$map, stringId=$stringId, params=$params)"

    companion object {
        val EMPTY = EnumDefinition()
    }
}
