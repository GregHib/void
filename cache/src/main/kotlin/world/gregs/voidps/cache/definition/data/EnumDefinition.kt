package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition


data class EnumDefinition(
    override var id: Int = -1,
    var keyType: Char = 0.toChar(),
    var valueType: Char = 0.toChar(),
    var defaultString: String = "null",
    var defaultInt: Int = 0,
    var length: Int = 0,
    var map: HashMap<Int, Any>? = null
) : Definition {
    fun getKey(value: Any) = map?.filterValues { it == value }?.keys?.firstOrNull() ?: -1

    fun getInt(id: Int) = map?.get(id) as? Int ?: defaultInt

    fun getString(id: Int) = map?.get(id) as? String ?: defaultString

}