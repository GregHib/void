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
        operator fun invoke(
            map: Map<String, Any>,
            type: VariableType
        ) = when (type) {
            VariableType.Varcstr -> build(map, type, format = VariableFormat.STRING, values = null)
            VariableType.Custom -> build(map, type, id = -1, transmit = false)
            else -> build(map, type)
        }

        private fun build(
            map: Map<String, Any>,
            type: VariableType,
            id: Int? = map["id"] as? Int,
            format: VariableFormat? = (map["format"] as? String)?.let { VariableFormat.byName(it) },
            defaultValue: Any? = map["default"],
            persistent: Boolean = map["persist"] as? Boolean ?: false,
            transmit: Boolean = map["transmit"] as? Boolean ?: true,
            values: Any? = map["values"]
        ): VariableDefinition {
            if (transmit) {
                checkNotNull(id) { "Transmitted variables must have an id. $type $map" }
                if (defaultValue == null && type != VariableType.Varcstr) {
                    checkNotNull(format) { "Transmitted variables must have a format. $type $map" }
                }
            }
            val variableFormat = format ?: if (defaultValue != null) VariableFormat.byName(defaultValue.javaClass.kotlin.simpleName!!) else VariableFormat.NONE
            val default = defaultValue ?: variableFormat.default(values)
            return VariableDefinition(id ?: -1, type, variableFormat, default, persistent, transmit, values)
        }

        val VariableDefinition?.persist: Boolean
            get() = this?.persistent ?: false
    }
}