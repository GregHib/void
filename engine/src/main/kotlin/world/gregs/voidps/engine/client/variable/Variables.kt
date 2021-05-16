package world.gregs.voidps.engine.client.variable

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.encode.sendVarbit
import world.gregs.voidps.network.encode.sendVarc
import world.gregs.voidps.network.encode.sendVarcStr
import world.gregs.voidps.network.encode.sendVarp

class Variables(
    val variables: MutableMap<String, Any> = mutableMapOf()
) {
    @JsonIgnore
    val temporaryVariables: MutableMap<String, Any> = mutableMapOf()
    @JsonIgnore
    lateinit var player: Player
    @JsonIgnore
    lateinit var manager: VariableManager

    fun <T : Any> set(key: String, value: T, refresh: Boolean) {
        val variable = manager.get(key) as? Variable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }
        val store = player.store(variable)
        store.set(key, variable, value)
        if (refresh) {
            send(key)
        }
    }

    fun send(key: String) {
        val variable = manager.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val store = player.store(variable)
        variable.send(store, key)
    }

    fun <T : Any> get(key: String): T {
        val variable = manager.get(key) as? Variable<T> ?: throw IllegalArgumentException("Unknown variable '$key'")
        val store = player.store(variable)
        return store.get(key, variable)
    }

    fun <T : Any> get(key: String, default: T): T {
        val variable = manager.get(key) as? Variable<T> ?: return default
        val store = player.store(variable)
        return store.get(key, variable)
    }

    fun <T : Any> add(key: String, id: T, refresh: Boolean) {
        val variable = manager.get(key) as? BitwiseVar<T> ?: return logger.debug { "Cannot find variable for key '$key'" }
        val store = player.store(variable)

        val power = variable.getValue(id) ?: return logger.debug { "Invalid bitwise value '$id'" }
        val value = store.get(key, variable)

        if (!value.has(power)) {//If isn't already added
            store.set(key, variable, value + power)//Add
            if (refresh) {
                send(key)
            }
        }
    }

    fun <T : Any> remove(key: String, id: T, refresh: Boolean) {
        val variable = manager.get(key) as? BitwiseVariable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }
        val store = player.store(variable)

        val power = variable.getValue(id) ?: return logger.debug { "Invalid bitwise value '$id'" }
        val value = store.get(key, variable)

        if (value.has(power)) {//If is added
            store.set(key, variable, value - power)//Remove
            if (refresh) {
                send(key)
            }
        }
    }

    fun <T : Any> has(key: String, id: T): Boolean {
        val variable = manager.get(key) as? BitwiseVariable<T> ?: return false
        val store = player.store(variable)

        val power = variable.getValue(id) ?: return false
        val value = store.get(key, variable)

        return value.has(power)
    }

    internal fun <T : Any> Variable<T>.send(store: MutableMap<String, Any>, key: String) {
        val value = store.get(key, this)
        when (type) {
            Variable.Type.VARP -> player.sendVarp(id, toInt(value))
            Variable.Type.VARBIT -> player.sendVarbit(id, toInt(value))
            Variable.Type.VARC -> player.sendVarc(id, toInt(value))
            Variable.Type.VARCSTR -> player.sendVarcStr(id, value as String)
        }
    }

    companion object {
        private val logger = InlineLogger()

        private fun Player.store(variable: Variable<*>): MutableMap<String, Any> = if (variable.persistent) variables.variables else variables.temporaryVariables

        /**
         * Checks if value [this] contains value [power]
         */
        private fun Int.has(power: Int) = (this and power) != 0

        /**
         * Gets Player variables current value or [variable] default
         */
        private fun <T : Any> MutableMap<String, Any>.get(key: String, variable: Variable<T>): T {
            return this[key] as? T ?: variable.defaultValue
        }

        /**
         * Sets Player variables value, removes if [variable] default
         */
        private fun <T : Any> MutableMap<String, Any>.set(key: String, variable: Variable<T>, value: T) {
            if (value == variable.defaultValue) {
                remove(key)
            } else {
                this[key] = value
            }
        }
    }
}