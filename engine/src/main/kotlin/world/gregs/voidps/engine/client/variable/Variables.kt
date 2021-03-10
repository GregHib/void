package world.gregs.voidps.engine.client.variable

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.encode.sendVarbit
import world.gregs.voidps.network.encode.sendVarc
import world.gregs.voidps.network.encode.sendVarcStr
import world.gregs.voidps.network.encode.sendVarp
import world.gregs.voidps.utility.get

val variablesModule = module {
    single { Variables() }
}

@Suppress("UNCHECKED_CAST")
class Variables {
    private val variables = mutableMapOf<String, Variable<*>>()

    fun <T : Any> set(player: Player, key: String, value: T, refresh: Boolean) {
        val variable = variables[key] as? Variable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }
        val store = player.store(variable)
        store.set(key, variable, value)
        if (refresh) {
            send(player, key)
        }
    }

    fun send(player: Player, key: String) {
        val variable = variables[key] ?: return logger.debug { "Cannot find variable for key '$key'" }
        val store = player.store(variable)
        variable.send(player, store, key)
    }

    fun <T : Any> get(player: Player, key: String): T {
        val variable = variables[key] as? Variable<T> ?: throw IllegalArgumentException("Unknown variable '$key'")
        val store = player.store(variable)
        return store.get(key, variable)
    }

    fun <T : Any> get(player: Player, key: String, default: T): T {
        val variable = variables[key] as? Variable<T> ?: return default
        val store = player.store(variable)
        return store.get(key, variable)
    }

    fun <T : Any> add(player: Player, key: String, id: T, refresh: Boolean) {
        val variable = variables[key] as? BitwiseVar<T> ?: return logger.debug { "Cannot find variable for key '$key'" }
        val store = player.store(variable)

        val power = variable.getValue(id) ?: return logger.debug { "Invalid bitwise value '$id'" }
        val value = store.get(key, variable)

        if (!value.has(power)) {//If isn't already added
            store.set(key, variable, value + power)//Add
            if (refresh) {
                send(player, key)
            }
        }
    }

    fun <T : Any> remove(player: Player, key: String, id: T, refresh: Boolean) {
        val variable = variables[key] as? BitwiseVariable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }
        val store = player.store(variable)

        val power = variable.getValue(id) ?: return logger.debug { "Invalid bitwise value '$id'" }
        val value = store.get(key, variable)

        if (value.has(power)) {//If is added
            store.set(key, variable, value - power)//Remove
            if (refresh) {
                send(player, key)
            }
        }
    }

    fun <T : Any> has(player: Player, key: String, id: T): Boolean {
        val variable = variables[key] as? BitwiseVariable<T> ?: return false
        val store = player.store(variable)

        val power = variable.getValue(id) ?: return false
        val value = store.get(key, variable)

        return value.has(power)
    }

    internal fun <T : Any> Variable<T>.send(player: Player, store: MutableMap<String, Any>, key: String) {
        val value = store.get(key, this)
        when (type) {
            Variable.Type.VARP -> player.sendVarp(id, toInt(value))
            Variable.Type.VARBIT -> player.sendVarbit(id, toInt(value))
            Variable.Type.VARC -> player.sendVarc(id, toInt(value))
            Variable.Type.VARCSTR -> player.sendVarcStr(id, value as String)
        }
    }

    fun register(name: String, variable: Variable<*>) {
        variables[name] = variable
    }

    fun clear() {
        variables.clear()
    }

    companion object {
        private val logger = InlineLogger()

        private fun Player.store(variable: Variable<*>): MutableMap<String, Any> = if (variable.persistent) variables else temporaryVariables

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

fun <T : Any> Player.setVar(key: String, value: T, refresh: Boolean = true) =
    get<Variables>().set(this, key, value, refresh)

fun Player.sendVar(key: String) = get<Variables>().send(this, key)

fun <T : Any> Player.addVar(key: String, value: T, refresh: Boolean = true) =
    get<Variables>().add(this, key, value, refresh)

fun <T : Any> Player.removeVar(key: String, value: T, refresh: Boolean = true) =
    get<Variables>().remove(this, key, value, refresh)

fun Player.toggleVar(key: String, refresh: Boolean = true): Boolean {
    val variables: Variables = get()
    val value = variables.get(this, key, false)
    variables.set(this, key, !value, refresh)
    return !value
}

fun Player.incVar(key: String, refresh: Boolean = true): Int {
    val variables: Variables = get()
    val value: Int = variables.get(this, key)
    variables.set(this, key, value + 1, refresh)
    return value + 1
}

fun Player.decVar(key: String, refresh: Boolean = true): Int {
    val variables: Variables = get()
    val value: Int = variables.get(this, key)
    variables.set(this, key, value - 1, refresh)
    return value - 1
}

fun <T : Any> Player.hasVar(key: String, id: T): Boolean {
    val variables: Variables = get()
    return variables.has(this, key, id)
}

fun <T : Any> Player.getVar(key: String, default: T): T {
    val variables: Variables = get()
    return variables.get(this, key, default)
}

fun <T : Any> Player.getVar(key: String): T {
    val variables: Variables = get()
    return variables.get(this, key)
}