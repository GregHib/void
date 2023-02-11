package world.gregs.voidps.engine.client.variable

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.sendVarbit
import world.gregs.voidps.engine.client.sendVarc
import world.gregs.voidps.engine.client.sendVarcStr
import world.gregs.voidps.engine.client.sendVarp
import world.gregs.voidps.engine.data.serial.MapSerializer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.data.definition.extra.VariableDefinitions
import world.gregs.voidps.engine.data.definition.config.VariableDefinition

@Suppress("UNCHECKED_CAST", "DuplicatedCode")
class Variables(
    @JsonSerialize(using = MapSerializer::class)
    val variables: MutableMap<String, Any> = mutableMapOf()
) {
    @JsonIgnore
    val temporaryVariables: MutableMap<String, Any> = mutableMapOf()

    @JsonIgnore
    private lateinit var player: Player

    @JsonIgnore
    private lateinit var definitions: VariableDefinitions

    fun link(player: Player, definitions: VariableDefinitions) {
        this.player = player
        this.definitions = definitions
    }

    fun set(key: String, value: Any, refresh: Boolean) {
        val variable = definitions.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val previous = get(key, variable.defaultValue)
        set(key, variable, value)
        if (refresh) {
            send(key)
        }
        player.events.emit(VariableSet(key, previous, value))
    }

    fun send(key: String) {
        val variable = definitions.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        variable.send(key)
    }

    fun <T : Any> get(key: String): T {
        val variable = definitions.getValue(key)
        return get(key, variable)
    }

    fun <T : Any> get(key: String, default: T): T {
        val variable = definitions.get(key) ?: return default
        return get(key, variable)
    }

    fun getIntValue(type: VariableType, id: Int): Int? {
        val key = definitions.getKey(type, id) ?: return null
        val variable = definitions.get(key) ?: return null
        val value = get<Any>(key, variable)
        return variable.toInt(value)
    }

    fun add(key: String, id: Any, refresh: Boolean) {
        val variable = definitions.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val power = variable.getValue(id) ?: return logger.debug { "Invalid bitwise value '$id'" }
        val value = get(key, variable) as Int
        if (!value.has(power)) {// If isn't already added
            set(key, variable, value + power)// Add
            if (refresh) {
                send(key)
            }
            player.events.emit(VariableAdded(key, id, value, value + power))
        }
    }

    fun remove(key: String, id: Any, refresh: Boolean) {
        val variable = definitions.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val power = variable.getValue(id) ?: return logger.debug { "Invalid bitwise value '$id'" }
        val value = get(key, variable) as Int
        if (value.has(power)) {// If is added
            set(key, variable, value - power)// Remove
            if (refresh) {
                send(key)
            }
            player.events.emit(VariableRemoved(key, id, value, value - power))
        }
    }

    fun clear(key: String, refresh: Boolean) {
        val variable = definitions.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val previous = get(key, variable.defaultValue)
        set(key, variable, variable.defaultValue)
        if (refresh) {
            send(key)
        }
        player.events.emit(VariableSet(key, previous, variable.defaultValue))
    }

    /**
     * @return whether [id] is active for [key]
     */
    fun has(key: String, id: Any): Boolean {
        val variable = definitions.get(key) ?: return false
        val power = variable.getValue(id) ?: return false
        val value = get(key, variable) as Int
        return value.has(power)
    }

    /**
     * @return whether [id] is a valid value in [key]
     */
    fun contains(key: String, id: Any): Boolean {
        val variable = definitions.get(key) ?: return false
        variable.getValue(id) ?: return false
        return true
    }

    internal fun VariableDefinition.send(key: String) {
        val value = get(key, defaultValue)
        when (type) {
            VariableType.Varp -> player.sendVarp(id, format.toInt(this, value))
            VariableType.Varbit -> player.sendVarbit(id, format.toInt(this, value))
            VariableType.Varc -> player.sendVarc(id, format.toInt(this, value))
            VariableType.Varcstr -> player.sendVarcStr(id, value as String)
        }
    }

    private fun store(variable: VariableDefinition): MutableMap<String, Any> =
        if (variable.persistent) variables else temporaryVariables

    /**
     * Gets Player variables current value or [variable] default
     */
    private fun <T : Any> get(key: String, variable: VariableDefinition): T {
        return store(variable)[key] as? T ?: variable.defaultValue as T
    }

    /**
     * Sets Player variables value, removes if [variable] default
     */
    private fun set(key: String, variable: VariableDefinition, value: Any) {
        if (value == variable.defaultValue) {
            store(variable).remove(key)
        } else {
            store(variable)[key] = value
        }
    }

    companion object {
        private val logger = InlineLogger()
    }
}

/**
 * Checks if value [this] contains value [power]
 */
fun Int.has(power: Int) = (this and power) != 0

fun Player.setVar(key: String, value: Any, refresh: Boolean = true) =
    variables.set(key, value, refresh)

fun Player.sendVar(key: String) = variables.send(key)

fun Player.addVar(key: String, value: Any, refresh: Boolean = true) =
    variables.add(key, value, refresh)

fun Player.removeVar(key: String, value: Any, refresh: Boolean = true) =
    variables.remove(key, value, refresh)

fun Player.clearVar(key: String, refresh: Boolean = true) =
    variables.clear(key, refresh)

fun Player.toggleVar(key: String, refresh: Boolean = true): Boolean {
    val value = variables.get(key, false)
    variables.set(key, !value, refresh)
    return !value
}

fun Player.incVar(key: String, amount: Int = 1, refresh: Boolean = true): Int {
    val value: Int = variables.get(key)
    variables.set(key, value + amount, refresh)
    return value + amount
}

fun Player.decVar(key: String, amount: Int = 1, refresh: Boolean = true): Int {
    val value: Int = variables.get(key)
    variables.set(key, value - amount, refresh)
    return value - amount
}

fun Player.containsVar(key: String, id: Any): Boolean {
    return variables.contains(key, id)
}

fun Player.hasVar(key: String, id: Any): Boolean {
    return variables.has(key, id)
}

fun <T : Any> Player.getVar(key: String, default: T): T {
    return variables.get(key, default)
}

fun <T : Any> Player.getVar(key: String): T {
    return variables.get(key)
}