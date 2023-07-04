package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.data.config.VariableDefinition.Companion.persist
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.Client

class PlayerVariables(
    events: Events,
    data: MutableMap<String, Any>,
    var definitions: VariableDefinitions = VariableDefinitions(),
    val temp: MutableMap<String, Any> = mutableMapOf()
) : Variables(events, data) {

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
        variable.send(client ?: return, value)
    }

    override fun data(key: String): MutableMap<String, Any> {
        return if (definitions.get(key).persist) data else temp
    }
}