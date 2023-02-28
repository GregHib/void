package world.gregs.voidps.engine.data.definition.config

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.engine.client.variable.StringValues
import world.gregs.voidps.engine.client.variable.VariableType
import world.gregs.voidps.engine.client.variable.VariableValues

data class VariableDefinition(
    override var id: Int,
    val type: VariableType,
    val values: VariableValues,
    val defaultValue: Any?,
    val persistent: Boolean,
    val transmit: Boolean,
) : Definition {

    companion object {
        operator fun invoke(
            map: Map<String, Any>,
            type: VariableType,
        ): VariableDefinition {
            val id = if (type == VariableType.Custom) -1 else map["id"] as? Int
            val transmit = if (type == VariableType.Custom) false else map["transmit"] as? Boolean ?: true
            val format = map["format"] as? String
            val default = map["default"]
            if (transmit) {
                checkNotNull(id) { "Transmitted variables must have an id. $type $map" }
                if (type != VariableType.Varcstr) {
                    checkNotNull(format ?: default) { "Transmitted variables must have a format or default value $type $map" }
                }
            }
            val persistent: Boolean = map["persist"] as? Boolean ?: false
            val values = if (type == VariableType.Varcstr) StringValues else {
                VariableValues(map["values"], format, default)
            }
            return VariableDefinition(id ?: -1, type, values, default ?: values.default(), persistent, transmit)
        }

        val VariableDefinition?.persist: Boolean
            get() = this?.persistent ?: false
    }
}