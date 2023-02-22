package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.player.Player

class VariableBits(
    private val variables: Variables
) {
    private lateinit var player: Player

    fun link(player: Player) {
        this.player = player
    }

    fun contains(key: String, id: Any): Boolean {
        val value: ArrayList<Any> = variables.getOrNull(key) ?: return false
        return value.contains(id)
    }

    fun set(key: String, id: Any, refresh: Boolean) {
        val value: ArrayList<Any> = variables.getOrPut(key) { arrayListOf(id) }
        if (!value.contains(id) && value.add(id)) {
            if (refresh) {
                variables.send(key)
            }
            player.events.emit(VariableAdded(key, id))
        }
    }

    fun remove(key: String, id: Any, refresh: Boolean) {
        val value: ArrayList<Any> = variables.getOrNull(key) ?: return
        if (value.remove(id)) {
            if (refresh) {
                variables.send(key)
            }
            player.events.emit(VariableRemoved(key, id))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun clear(key: String, refresh: Boolean) {
        val values = variables.clear(key, refresh) as? ArrayList<Any> ?: return
        for (value in values) {
            player.events.emit(VariableRemoved(key, value))
        }
        if (refresh) {
            variables.send(key)
        }
    }
}