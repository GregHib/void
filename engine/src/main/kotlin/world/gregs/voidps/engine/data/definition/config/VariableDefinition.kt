package world.gregs.voidps.engine.data.definition.config

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.engine.client.variable.VariableFormat
import world.gregs.voidps.engine.client.variable.VariableType

@Suppress("UNCHECKED_CAST")
data class VariableDefinition(
    override var id: Int,
    val type: VariableType,
    val format: VariableFormat,
    val defaultValue: Any?,
    val persistent: Boolean,
    val transmit: Boolean,
    val values: Any?
) : Definition {

    init {
        if (format == VariableFormat.MAP) {
            val map = values as Map<Any, Int>
            check(map.containsKey(defaultValue)) { "Values must contain default '$defaultValue'" }
        }
    }

    fun toInt(key: Any): Int = format.toInt(this, key)

    fun getValue(key: Any): Int? = format.getValue(this, key)

    companion object {
        operator fun invoke(map: Map<String, Any>): VariableDefinition {
            val transmit = map["transmit"] as? Boolean ?: true
            if (transmit) {
                check(map.containsKey("id")) { "Transmitted variables must have an id. $map" }
                check(map.containsKey("type")) { "Transmitted variables must have a type. $map" }
                if (!map.containsKey("default") && map["type"] != "varcstr") {
                    check(map.containsKey("format")) { "Transmitted variables must have a format. $map" }
                }
            } else {
                if (!map.containsKey("default")) {
                    check(map.containsKey("format")) { "Custom variables must have a format. $map" }
                }
                if (map.containsKey("persist")) {
                    check(map["persist"] as Boolean) { "It's unnecessary to document non-persistent custom variables. $map" }
                }
            }
            val id = map["id"] as? Int ?: -1
            var default = map["default"]
            val type = if (transmit) VariableType.byName(map["type"] as String) else VariableType.Custom
            val format = when {
                type == VariableType.Varcstr -> VariableFormat.STRING
                transmit -> VariableFormat.byName(map["format"] as? String ?: default?.javaClass?.kotlin?.simpleName ?: "none")
                else -> VariableFormat.NONE
            }
            val values = map["values"]
            default = format.default(values)
            val persist = if (transmit) map["persist"] as? Boolean ?: false else true
            return VariableDefinition(id, type, format, default, persist, transmit, values)
        }

        val VariableDefinition?.persist: Boolean
            get() = this?.persistent ?: false
    }
}