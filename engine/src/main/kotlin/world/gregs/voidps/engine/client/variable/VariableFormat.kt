package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.definition.VariableDefinition
import world.gregs.voidps.utility.func.toInt

/**
 * [VariableDefinition] formats for converting usable values into [Int]'s
 */
@Suppress("UNCHECKED_CAST")
enum class VariableFormat(
    val default: (Any?) -> Any = { 0 },
    val toInt: (VariableDefinition, Any) -> Int = { _, value -> value as Int },
    val getValue: (VariableDefinition, Any) -> Int? = { _, _ -> null }
) {
    INT,
    STRING(
        default = { "" }
    ),
    MAP(
        default = { values -> (values as Map<Any, Int>).keys.first() },
        toInt = { def, value -> (def.values as Map<Any, Int>)[value] ?: -1 }
    ),
    LIST(
        default = { values -> (values as List<Any>).first() },
        toInt = { def, value -> (def.values as List<Any>).indexOf(value) }
    ),
    DOUBLE(
        default = { 0.0 },
        toInt = { _, value -> (value as Double).toInt() * 10 }
    ),
    BOOLEAN(
        default = { false },
        toInt = { _, value -> (value as Boolean).toInt() }
    ),
    INT_BOOLEAN(
        default = { false },
        toInt = { def, value -> (def.values as Map<String, Int>)[if (value as Boolean) "trueValue" else "falseValue"] ?: value.toInt() }
    ),
    NEG_BOOLEAN(
        default = { false },
        toInt = { _, value -> (!(value as Boolean)).toInt() }
    ),
    BITWISE(
        getValue = { def, key ->
            val index = (def.values as List<Any>).indexOf(key)
            if (index != -1) 1 shl index else null
        }
    );

    companion object {
        fun byName(name: String?) = values().firstOrNull { it.name.toLowerCase() == name }
    }
}