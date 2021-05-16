package world.gregs.voidps.engine.client.variable

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.encode.sendVarbit
import world.gregs.voidps.network.encode.sendVarc
import world.gregs.voidps.network.encode.sendVarcStr
import world.gregs.voidps.network.encode.sendVarp

@Suppress("UNCHECKED_CAST")
class Variables(
    val variables: MutableMap<String, Any> = mutableMapOf()
) {
    @JsonIgnore
    val temporaryVariables: MutableMap<String, Any> = mutableMapOf()

    @JsonIgnore
    private lateinit var player: Player

    @JsonIgnore
    private lateinit var store: VariableStore

    fun link(player: Player, store: VariableStore) {
        this.player = player
        this.store = store
    }

    fun <T : Any> set(key: String, value: T, refresh: Boolean) {
        val variable = store.get(key) as? Variable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }
        set(key, variable, value)
        if (refresh) {
            send(key)
        }
        player.events.emit(VariableSet(key, value))
    }

    fun send(key: String) {
        val variable = store.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        variable.send(key)
    }

    fun <T : Any> get(key: String): T {
        val variable = store.get(key) as Variable<T>
        return get(key, variable)
    }

    fun <T : Any> get(key: String, default: T): T {
        val variable = store.get(key) as? Variable<T> ?: return default
        return get(key, variable)
    }

    fun <T : Any> add(key: String, id: T, refresh: Boolean) {
        val variable = store.get(key) as? BitwiseVar<T> ?: return logger.debug { "Cannot find variable for key '$key'" }

        val power = variable.getValue(id) ?: return logger.debug { "Invalid bitwise value '$id'" }
        val value = get(key, variable)

        if (!value.has(power)) {// If isn't already added
            set(key, variable, value + power)// Add
            if (refresh) {
                send(key)
            }
            player.events.emit(VariableAdded(key, id, value, value + power))
        }
    }

    fun <T : Any> remove(key: String, id: T, refresh: Boolean) {
        val variable = store.get(key) as? BitwiseVariable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }

        if (!remove(variable, key, id, refresh)) {
            logger.debug { "Invalid bitwise value '$id'" }
        }
    }

    fun <T : Any> clear(key: String, refresh: Boolean) {
        val variable = store.get(key) as? BitwiseVariable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }

        for (id in variable.values) {
            remove(variable, key, id, refresh)
        }
    }

    private fun <T : Any> remove(variable: BitwiseVariable<T>, key: String, id: T, refresh: Boolean): Boolean {
        val power = variable.getValue(id) ?: return false
        val value = get(key, variable)

        if (value.has(power)) {// If is added
            set(key, variable, value - power)// Remove
            if (refresh) {
                send(key)
            }
            player.events.emit(VariableRemoved(key, id, value, value - power))
        }
        return true
    }

    fun <T : Any> has(key: String, id: T): Boolean {
        val variable = store.get(key) as? BitwiseVariable<T> ?: return false
        val power = variable.getValue(id) ?: return false
        val value = get(key, variable)
        return value.has(power)
    }

    internal fun <T : Any> Variable<T>.send(key: String) {
        val value = get(key, this)
        when (type) {
            Variable.Type.VARP -> player.sendVarp(id, toInt(value))
            Variable.Type.VARBIT -> player.sendVarbit(id, toInt(value))
            Variable.Type.VARC -> player.sendVarc(id, toInt(value))
            Variable.Type.VARCSTR -> player.sendVarcStr(id, value as String)
        }
    }

    private fun store(variable: Variable<*>): MutableMap<String, Any> = if (variable.persistent) variables else temporaryVariables

    /**
     * Gets Player variables current value or [variable] default
     */
    private fun <T : Any> get(key: String, variable: Variable<T>): T {
        return store(variable)[key] as? T ?: variable.defaultValue
    }

    /**
     * Sets Player variables value, removes if [variable] default
     */
    private fun <T : Any> set(key: String, variable: Variable<T>, value: T) {
        if (value == variable.defaultValue) {
            store(variable).remove(key)
        } else {
            store(variable)[key] = value
        }
    }

    companion object {
        private val logger = InlineLogger()

        /**
         * Checks if value [this] contains value [power]
         */
        private fun Int.has(power: Int) = (this and power) != 0

    }
}

fun <T : Any> Player.setVar(key: String, value: T, refresh: Boolean = true) =
    variables.set(key, value, refresh)

fun Player.sendVar(key: String) = variables.send(key)

fun <T : Any> Player.addVar(key: String, value: T, refresh: Boolean = true) =
    variables.add(key, value, refresh)

fun <T : Any> Player.removeVar(key: String, value: T, refresh: Boolean = true) =
    variables.remove(key, value, refresh)

fun <T : Any> Player.clearVar(key: String, refresh: Boolean = true) =
    variables.clear<T>(key, refresh)

fun Player.toggleVar(key: String, refresh: Boolean = true): Boolean {
    val value = variables.get(key, false)
    variables.set(key, !value, refresh)
    return !value
}

fun Player.incVar(key: String, refresh: Boolean = true): Int {
    val value: Int = variables.get(key)
    variables.set(key, value + 1, refresh)
    return value + 1
}

fun Player.decVar(key: String, refresh: Boolean = true): Int {
    val value: Int = variables.get(key)
    variables.set(key, value - 1, refresh)
    return value - 1
}

fun <T : Any> Player.hasVar(key: String, id: T): Boolean {
    return variables.has(key, id)
}

fun <T : Any> Player.getVar(key: String, default: T): T {
    return variables.get(key, default)
}

fun <T : Any> Player.getVar(key: String): T {
    return variables.get(key)
}