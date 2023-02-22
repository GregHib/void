package world.gregs.voidps.engine.client.variable

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.definition.extra.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player

class VariableBits(
    private val variables: Variables,
) {
    private lateinit var player: Player
    private var definitions: VariableDefinitions? = null

    fun link(player: Player, definitions: VariableDefinitions) {
        this.player = player
        this.definitions = definitions
    }

    fun set(key: String, id: Any, refresh: Boolean) {
        val variable = definitions?.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val value = variables.getOrNull<ArrayList<Any>>(key, variable)
        if (value == null || !value.contains(id)) {
            if (value == null) {
                variables.store(variable.persistent)[key] = arrayListOf(id)
            } else {
                value.add(id)
            }
            if (refresh) {
                variables.send(key)
            }
            player.events.emit(VariableAdded(key, id))
        }
    }

    fun remove(key: String, id: Any, refresh: Boolean) {
        val variable = definitions?.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val value = variables.getOrNull<ArrayList<Any>>(key, variable)
        if (value != null && value.contains(id)) {
            value.remove(id)
            if (refresh) {
                variables.send(key)
            }
            player.events.emit(VariableRemoved(key, id))
        }
    }

    fun contains(key: String, id: Any): Boolean {
        val variable = definitions?.get(key) ?: return false
        val value = variables.get(key, variable) as ArrayList<Any>
        return value.contains(id)
    }

    companion object {
        val logger = InlineLogger()
    }
}