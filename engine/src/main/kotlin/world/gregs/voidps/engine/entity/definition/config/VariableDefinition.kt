package world.gregs.voidps.engine.entity.definition.config

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.engine.client.variable.VariableFormat
import world.gregs.voidps.engine.client.variable.VariableType

@Suppress("UNCHECKED_CAST")
data class VariableDefinition(
    override var id: Int,
    val type: VariableType,
    val format: VariableFormat,
    val defaultValue: Any,
    val persistent: Boolean,
    val values: Any
) : Definition {

    init {
        if (format == VariableFormat.MAP) {
            val map = values as Map<Any, Int>
            check(map.containsKey(defaultValue)) { "Values must contain default '$defaultValue'" }
        }
    }

    fun getValue(key: Any): Int? = format.getValue(this, key)

    companion object {
        operator fun invoke(map: Map<String, Any>): VariableDefinition {
            val id = map["id"] as Int
            val type = VariableType.byName(map["type"] as? String) ?: VariableType.Varc
            val format = VariableFormat.byName(map["format"] as? String) ?: VariableFormat.INT
            val values = map["values"]
            val default = map["default"] ?: format.default(values)
            val persist = map["persist"] as? Boolean ?: false

            println("Read $id $format $values $default $map")

            return VariableDefinition(id, type, format, default, persist, values ?: 0)
        }
    }
}