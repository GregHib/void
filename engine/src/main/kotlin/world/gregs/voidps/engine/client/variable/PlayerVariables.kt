package world.gregs.voidps.engine.client.variable

import com.fasterxml.jackson.annotation.JsonIgnore
import world.gregs.voidps.engine.data.definition.config.VariableDefinition.Companion.persist
import world.gregs.voidps.engine.data.definition.extra.VariableDefinitions
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.sendVarbit
import world.gregs.voidps.network.encode.sendVarc
import world.gregs.voidps.network.encode.sendVarcStr
import world.gregs.voidps.network.encode.sendVarp

class PlayerVariables(
    events: Events,
    data: VariableData,
    @JsonIgnore
    var definitions: VariableDefinitions = VariableDefinitions()
) : Variables(events, data) {

    constructor(events: Events, map: MutableMap<String, Any>) : this(events, VariableData(map))

    @JsonIgnore
    var client: Client? = null

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: String): T {
        val variable = definitions.get(key)
        return (super.getOrNull(key) ?: variable?.defaultValue) as T
    }

    override fun set(key: String, value: Any, refresh: Boolean) {
        val variable = definitions.get(key)
        if (value == variable?.defaultValue) {
            clear(key, refresh)
            return
        }
        super.set(key, value, refresh)
    }

    override fun send(key: String) {
        val variable = definitions.get(key) ?: return
        if (!variable.transmit) {
            return
        }
        val value = getOrNull(key) ?: variable.defaultValue ?: return
        when (variable.type) {
            VariableType.Varp -> client?.sendVarp(variable.id, variable.values.toInt(value))
            VariableType.Varbit -> client?.sendVarbit(variable.id, variable.values.toInt(value))
            VariableType.Varc -> client?.sendVarc(variable.id, variable.values.toInt(value))
            VariableType.Varcstr -> client?.sendVarcStr(variable.id, value as String)
            VariableType.Custom -> return
        }
    }

    override fun persist(key: String) {
        data.persist = definitions.get(key).persist
    }
}