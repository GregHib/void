package world.gregs.voidps.engine.data.config

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.engine.client.variable.StringValues
import world.gregs.voidps.engine.client.variable.VariableValues
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.sendVarbit
import world.gregs.voidps.network.encode.sendVarc
import world.gregs.voidps.network.encode.sendVarcStr
import world.gregs.voidps.network.encode.sendVarp

open class VariableDefinition internal constructor(
    override var id: Int,
    val values: VariableValues,
    val defaultValue: Any?,
    val persistent: Boolean,
    val transmit: Boolean,
) : Definition {

    constructor(
        map: Map<String, Any?>,
        id: Int = map["id"] as Int,
        values: VariableValues = VariableValues(map["values"], map["format"] as? String, map["default"]),
        defaultValue: Any? = map["default"] ?: values.default(),
        persistent: Boolean = map["persist"] as? Boolean ?: false,
        transmit: Boolean = map["transmit"] as? Boolean ?: true
    ) : this(id, values, defaultValue, persistent, transmit)

    class VarbitDefinition(map: Map<String, Any?>) : VariableDefinition(map)
    class VarpDefinition(map: Map<String, Any?>) : VariableDefinition(map)
    class VarcDefinition(map: Map<String, Any?>) : VariableDefinition(map)
    class VarcStrDefinition(map: Map<String, Any?>) : VariableDefinition(map, values = StringValues)
    class CustomVariableDefinition(map: Map<String, Any?>) : VariableDefinition(map, id = -1, transmit = false)

    fun send(client: Client, value: Any) {
        try {
            when (this) {
                is VarpDefinition -> client.sendVarp(id, values.toInt(value))
                is VarbitDefinition -> client.sendVarbit(id, values.toInt(value))
                is VarcDefinition -> client.sendVarc(id, values.toInt(value))
                is VarcStrDefinition -> client.sendVarcStr(id, value as String)
                else -> return
            }
        } catch (e: Exception) {
            logger.warn(e) { "Error sending variable $id '$value'" }
        }
    }

    companion object {
        private val logger = InlineLogger()
        fun varbit(): (Map<String, Any>) -> VariableDefinition = { VarbitDefinition(it) }
        fun varp(): (Map<String, Any>) -> VariableDefinition = { VarpDefinition(it) }
        fun varc(): (Map<String, Any>) -> VariableDefinition = { VarcDefinition(it) }
        fun varcStr(): (Map<String, Any>) -> VariableDefinition = { VarcStrDefinition(it) }
        fun custom(): (Map<String, Any>) -> VariableDefinition = { CustomVariableDefinition(it) }

        val VariableDefinition?.persist: Boolean
            get() = this?.persistent ?: false
    }
}