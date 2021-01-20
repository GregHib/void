package world.gregs.void.engine.client.variable

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerVariables
import world.gregs.void.network.codec.game.encode.sendVarbit
import world.gregs.void.network.codec.game.encode.sendVarc
import world.gregs.void.network.codec.game.encode.sendVarcStr
import world.gregs.void.network.codec.game.encode.sendVarp
import world.gregs.void.utility.get

val variablesModule = module {
    single { Variables() }
}

@Suppress("UNCHECKED_CAST")
class Variables {
    val names = mutableMapOf<String, Int>()
    val variables = mutableMapOf<Int, Variable<*>>()

    fun removed(player: Player) {
        player.variables.forEach { (hash, value) ->
            val variable = variables[hash]!!
            if (variable.persistent) {
                println("Save ${names.entries.firstOrNull { it.value == hash }} $value")
            }
        }
    }

    fun <T : Any> set(player: Player, key: String, value: T, refresh: Boolean) {
        val store = player.variables
        val variable = variables[key] as? Variable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }
        store.set(variable, value)
        if (refresh) {
            send(player, key)
        }
    }

    fun send(player: Player, key: String) {
        val store = player.variables
        val variable = variables[key] ?: return logger.debug { "Cannot find variable for key '$key'" }
        variable.send(player, store)
    }

    fun <T : Any> get(player: Player, key: String): T {
        val store = player.variables
        val variable = variables[key] as? Variable<T> ?: throw IllegalArgumentException("Unknown variable '$key'")
        return store.get(variable)
    }

    fun <T : Any> get(player: Player, key: String, default: T): T {
        val store = player.variables
        val variable = variables[key] as? Variable<T> ?: return default
        return store.get(variable)
    }

    fun <T : Any> add(player: Player, key: String, id: T, refresh: Boolean) {
        val store = player.variables
        val variable = variables[key] as? BitwiseVar<T> ?: return logger.debug { "Cannot find variable for key '$key'" }

        val power = variable.getValue(id) ?: return logger.debug { "Invalid bitwise value '$id'" }
        val value = store.get(variable)

        if (!value.has(power)) {//If isn't already added
            store.set(variable, value + power)//Add
            if (refresh) {
                send(player, key)
            }
        }
    }

    fun <T : Any> remove(player: Player, key: String, id: T, refresh: Boolean) {
        val store = player.variables
        val variable = variables[key] as? BitwiseVariable<T> ?: return logger.debug { "Cannot find variable for key '$key'" }

        val power = variable.getValue(id) ?: return logger.debug { "Invalid bitwise value '$id'" }
        val value = store.get(variable)

        if (value.has(power)) {//If is added
            store.set(variable, value - power)//Remove
            if (refresh) {
                send(player, key)
            }
        }
    }

    fun <T : Any> has(player: Player, key: String, id: T): Boolean {
        val store = player.variables
        val variable = variables[key] as? BitwiseVariable<T> ?: return false

        val power = variable.getValue(id) ?: return false
        val value = store.get(variable)

        return value.has(power)
    }

    internal fun <T : Any> Variable<T>.send(player: Player, store: PlayerVariables) {
        val value = store.get(this)
        when (type) {
            Variable.Type.VARP -> player.sendVarp(id, toInt(value))
            Variable.Type.VARBIT -> player.sendVarbit(id, toInt(value))
            Variable.Type.VARC -> player.sendVarc(id, toInt(value))
            Variable.Type.VARCSTR -> player.sendVarcStr(id, value as String)
        }
    }

    /**
     * Extension for [variables] to get using [names]
     */
    private operator fun <T : Any> Map<Int, T>.get(key: String): T? {
        return get(names[key])
    }

    companion object {
        private val logger = InlineLogger()

        /**
         * Checks if value [this] contains value [power]
         */
        private fun Int.has(power: Int) = (this and power) != 0

        /**
         * Gets [PlayerVariables]'s current value or [variable] default
         */
        private fun <T : Any> PlayerVariables.get(variable: Variable<T>): T {
            return this[variable.hash] as? T ?: variable.defaultValue
        }

        /**
         * Sets [PlayerVariables] value, removes if [variable] default
         */
        private fun <T : Any> PlayerVariables.set(variable: Variable<T>, value: T) {
            if (value == variable.defaultValue) {
                remove(variable.hash)
            } else {
                this[variable.hash] = value
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