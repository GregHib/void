package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class EnumDefinition(
    override var id: Int = -1,
    var keyType: Char = 0.toChar(),
    var valueType: Char = 0.toChar(),
    var defaultString: String = "null",
    var defaultInt: Int = 0,
    var length: Int = 0,
    var map: Map<Int, Any>? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Extra {
    fun getKey(value: Any) = map?.filterValues { it == value }?.keys?.lastOrNull() ?: -1

    fun getInt(id: Int) = map?.get(id) as? Int ?: defaultInt

    fun randomInt() = map?.values?.random() as? Int ?: defaultInt

    fun getString(id: Int) = map?.get(id) as? String ?: defaultString

    companion object {
        val EMPTY = EnumDefinition()
    }
}
