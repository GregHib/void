package world.gregs.voidps.engine.data.config

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.engine.client.variable.StringValues
import world.gregs.voidps.engine.client.variable.VariableValues
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.protocol.encode.sendVarbit
import world.gregs.voidps.network.login.protocol.encode.sendVarc
import world.gregs.voidps.network.login.protocol.encode.sendVarcStr
import world.gregs.voidps.network.login.protocol.encode.sendVarp

open class VariableDefinition internal constructor(
    override var id: Int,
    val values: VariableValues,
    val defaultValue: Any?,
    val persistent: Boolean,
    val transmit: Boolean,
) : Definition {

    class VarbitDefinition(id: Int, values: VariableValues, default: Any?, persistent: Boolean, transmit: Boolean) :
        VariableDefinition(id, values, default ?: values.default(), persistent, transmit)
    class VarpDefinition(id: Int, values: VariableValues, default: Any?, persistent: Boolean, transmit: Boolean) :
        VariableDefinition(id, values, default ?: values.default(), persistent, transmit)
    class VarcDefinition(id: Int, values: VariableValues, default: Any?, persistent: Boolean, transmit: Boolean) :
        VariableDefinition(id, values, default ?: values.default(), persistent, transmit)
    class VarcStrDefinition(id: Int, default: Any?, persistent: Boolean, transmit: Boolean) :
        VariableDefinition(id, StringValues, default ?: StringValues.default(), persistent, transmit)
    class CustomVariableDefinition(values: VariableValues, default: Any?, persistent: Boolean) :
        VariableDefinition(-1, values, default ?: values.default(), persistent, transmit = false)

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
        val VariableDefinition?.persist: Boolean
            get() = this?.persistent ?: false
    }
}