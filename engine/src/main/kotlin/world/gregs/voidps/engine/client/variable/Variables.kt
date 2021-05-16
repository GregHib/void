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
    lateinit var player: Player

    @JsonIgnore
    lateinit var store: VariableStore

    fun <T : Any> set(key: String, value: T, refresh: Boolean) {
        val variable = store.get(key) as? Variable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }
        set(key, variable, value)
        if (refresh) {
            send(key)
        }
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
        }
    }

    fun <T : Any> remove(key: String, id: T, refresh: Boolean) {
        val variable = store.get(key) as? BitwiseVariable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }

        val power = variable.getValue(id) ?: return logger.debug { "Invalid bitwise value '$id'" }
        val value = get(key, variable)

        if (value.has(power)) {// If is added
            set(key, variable, value - power)// Remove
            if (refresh) {
                send(key)
            }
        }
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