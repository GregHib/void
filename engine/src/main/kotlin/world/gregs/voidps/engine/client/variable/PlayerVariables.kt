package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.data.config.VariableDefinition.Companion.persist
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.Client

class PlayerVariables(
    player: Player,
    data: MutableMap<String, Any>,
    val temp: MutableMap<String, Any> = mutableMapOf(),
) : Variables(player, data) {

    var client: Client? = null

    override fun set(key: String, value: Any, refresh: Boolean) {
        val variable = VariableDefinitions.get(key)
        if (value == variable?.defaultValue) {
            clear(key, refresh)
            return
        }
        super.set(key, value, refresh)
    }

    override fun send(key: String) {
        val variable = VariableDefinitions.get(key) ?: return
        if (!variable.transmit) {
            return
        }
        val value = get(key) ?: variable.defaultValue ?: return
        variable.send(client ?: return, value)
    }

    override fun data(key: String): MutableMap<String, Any> = if (VariableDefinitions.get(key).persist) data else temp
}
