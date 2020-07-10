package rs.dusk.engine.model.engine.variable

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import rs.dusk.engine.client.send
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerVariables
import rs.dusk.network.rs.codec.game.encode.message.VarbitMessage
import rs.dusk.network.rs.codec.game.encode.message.VarcMessage
import rs.dusk.network.rs.codec.game.encode.message.VarcStrMessage
import rs.dusk.network.rs.codec.game.encode.message.VarpMessage
import rs.dusk.utility.get

val variablesModule = module {
    single { Variables() }
}

@Suppress("UNCHECKED_CAST")
class Variables {
    val names = mutableMapOf<String, Int>()
    val variables = mutableMapOf<Int, Variable<*>>()

    fun removed(player: Player) {
        // TODO saving, ideally only persistent values are serialized in Player.kt
        player.variables.forEach { (hash, value) ->
            val variable = variables[hash]!!
            if (variable.persistent) {
                println("Save ${names.entries.firstOrNull { it.value == hash }} $value")
            }
        }
    }

    fun <T : Any> set(player: Player, key: String, value: T, refresh: Boolean) {
        val store = player.variables
        val variable = variables[key] as? Variable<T> ?: return logger.warn { "Cannot variable for key '$key'" }
        store.set(variable, value)
        if (refresh) {
            send(player, key)
        }
    }

    fun send(player: Player, key: String) {
        val store = player.variables
        val variable = variables[key] ?: return logger.warn { "Cannot variable for key '$key'" }
        variable.send(player, store)
    }

    fun <T : Any> get(player: Player, key: String, default: T): T {
        val store = player.variables
        val variable = variables[key] as? Variable<T> ?: return default
        return store.get(variable)
    }

    fun <T : Any> add(player: Player, key: String, id: T, refresh: Boolean) {
        val store = player.variables
        val variable = variables[key] as? BitwiseVariable<T> ?: return logger.warn { "Cannot variable for key '$key'" }

        val power = variable.getPower(id) ?: return logger.warn { "Invalid bitwise value '$id'" }
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
        val variable = variables[key] as? BitwiseVariable<T> ?: return logger.warn { "Cannot variable for key '$key'" }

        val power = variable.getPower(id) ?: return logger.warn { "Invalid bitwise value '$id'" }
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

        val power = variable.getPower(id) ?: return false
        val value = store.get(variable)

        return value.has(power)
    }

    internal fun <T : Any> Variable<T>.send(player: Player, store: PlayerVariables) {
        val value = store.get(this)
        player.send(
            when (type) {
                Variable.Type.VARP -> VarpMessage(id, toInt(value))
                Variable.Type.VARBIT -> VarbitMessage(id, toInt(value))
                Variable.Type.VARC -> VarcMessage(id, toInt(value))
                Variable.Type.VARCSTR -> VarcStrMessage(id, value as String)
            }
        )
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
         * Checks a [BitwiseVariable] for [id] value
         * @return pow(2, index) or null if not found
         */
        private fun <T : Any> BitwiseVariable<T>.getPower(id: T): Int? {
            val index = values.indexOf(id)
            if (index == -1) {
                return null// Invalid value
            }
            return 1 shl index// Return power of 2 of the index
        }

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

fun Player.toggleVar(key: String, refresh: Boolean = true) {
    val variables: Variables = get()
    variables.set(this, key, !variables.get(this, key, false), refresh)
}