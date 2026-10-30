package world.gregs.voidps.engine.client.variable

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.config.VariableDefinition.Companion.persist
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.Client

class PlayerVariables(
    private val player: Player,
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
        val map = if (variable.persist) data else temp
        val previous = map[key]
        if (previous == value) {
            return
        }
        map[key] = value
        if (refresh && variable != null && variable.transmit) {
            send(key)
        }
        VariableApi.set(player, key, previous, value)
    }

    override fun clear(key: String, refresh: Boolean): Any? {
        val variable = VariableDefinitions.get(key)
        val removed = (if (variable.persist) data else temp).remove(key) ?: return null
        if (refresh && variable != null && variable.transmit) {
            send(key)
        }
        VariableApi.set(player, key, removed, null)
        return removed
    }

    override fun send(key: String) {
        val variable = VariableDefinitions.get(key) ?: return
        if (!variable.transmit) {
            return
        }
        val value = get(key) ?: variable.defaultValue ?: return
        variable.send(client ?: return, value)
    }

    override fun sendAll() {
        val client = client ?: return
        for ((key, variable) in VariableDefinitions.definitions) {
            if (!variable.transmit) {
                continue
            }
            val value = data[key]
            if (value != null) {
                if (value != variable.defaultValue) {
                    variable.send(client, value)
                }
                continue
            }
            // The client assumes zero for anything unsent
            if (variable !is VariableDefinition.VarpDefinition && variable !is VariableDefinition.VarbitDefinition) {
                continue
            }
            val default = variable.defaultValue ?: continue
            val int = try {
                variable.values.toInt(default)
            } catch (e: ClassCastException) {
                logger.warn(e) { "Invalid default '$default' for variable '$key'." }
                continue
            }
            if (int > 0) {
                variable.send(client, default)
            }
        }
    }

    override fun data(key: String): MutableMap<String, Any> = if (VariableDefinitions.get(key).persist) data else temp

    companion object {
        private val logger = InlineLogger()
    }
}
