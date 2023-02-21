package world.gregs.voidps.engine.client.variable

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.sendVarbit
import world.gregs.voidps.engine.client.sendVarc
import world.gregs.voidps.engine.client.sendVarcStr
import world.gregs.voidps.engine.client.sendVarp
import world.gregs.voidps.engine.data.definition.config.VariableDefinition
import world.gregs.voidps.engine.data.definition.extra.VariableDefinitions
import world.gregs.voidps.engine.data.serial.MapSerializer
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.timer.epochSeconds

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
    private var definitions: VariableDefinitions? = null

    fun link(player: Player, definitions: VariableDefinitions) {
        this.player = player
        this.definitions = definitions
    }

    fun set(key: String, value: Any, refresh: Boolean) {
        val variable = definitions?.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val previous = get(key, variable.defaultValue)
        set(key, variable, value)
        if (refresh) {
            send(key)
        }
        player.events.emit(VariableSet(key, previous, value))
    }

    fun send(key: String) {
        val variable = definitions?.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        variable.send(key)
    }

    fun <T : Any> get(key: String): T {
        val variable = definitions!!.getValue(key)
        return get(key, variable)
    }

    fun <T : Any> getOrNull(key: String): T? {
        val variable = definitions?.get(key) ?: return null
        return get(key, variable)
    }

    fun <T : Any> get(key: String, default: T): T {
        val variable = definitions?.get(key) ?: return default
        return get(key, variable)
    }

    fun getIntValue(type: VariableType, id: Int): Int? {
        val key = definitions?.getKey(type, id) ?: return null
        val variable = definitions!!.get(key) ?: return null
        val value = get<Any>(key, variable)
        return variable.toInt(value)
    }

    fun add(key: String, id: Any, refresh: Boolean) {
        val variable = definitions?.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val value = getOrNull<ArrayList<Any>>(key, variable)
        if (value == null || !value.contains(id)) {// If isn't already added
            if (value == null) {
                set(key, variable, arrayListOf(id))
            } else {
                value.add(id)
            }
            if (refresh) {
                send(key)
            }
            player.events.emit(VariableAdded(key, id))
        }
    }

    fun remove(key: String, id: Any, refresh: Boolean) {
        val variable = definitions?.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val value = getOrNull<ArrayList<Any>>(key, variable)
        if (value != null && value.contains(id)) {// If is added
            value.remove(id)
            if (refresh) {
                send(key)
            }
            player.events.emit(VariableRemoved(key, id))
        }
    }

    fun clear(key: String, refresh: Boolean) {
        val variable = definitions?.get(key) ?: return logger.debug { "Cannot find variable for key '$key'" }
        val previous = get(key, variable.defaultValue)
        set(key, variable, variable.defaultValue)
        if (refresh) {
            send(key)
        }
        player.events.emit(VariableSet(key, previous, variable.defaultValue))
    }

    fun has(key: String): Boolean {
        val variable = definitions?.get(key) ?: return false
        return store(variable).containsKey(key)
    }

    /**
     * @return whether [id] is active for [key]
     */
    fun has(key: String, id: Any): Boolean {
        val variable = definitions?.get(key) ?: return false
        val value = get(key, variable) as ArrayList<Any>
        return value.contains(id)
    }

    /**
     * @return whether [id] is a valid value in [key]
     */
    fun contains(key: String, id: Any): Boolean {
        val variable = definitions?.get(key) ?: return false
        variable.getValue(id) ?: return false
        return true
    }

    internal fun VariableDefinition.send(key: String) {
        if (!transmit) {
            return
        }
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
        return getOrNull(key, variable) ?: variable.defaultValue as T
    }

    private fun <T : Any> getOrNull(key: String, variable: VariableDefinition): T? {
        return store(variable)[key] as? T
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

fun Player.hasVar(key: String): Boolean {
    return variables.has(key)
}

fun <T : Any> Player.getVar(key: String, default: T): T {
    return variables.get(key, default)
}

fun <T : Any> Player.getVar(key: String): T {
    return variables.get(key)
}

fun Character.start(key: String, seconds: Int) {
    if(this is Player) {
        setVar(key, epochSeconds() + seconds)
    } else {
        this[key] = epochSeconds() + seconds
    }
}

fun Character.stop(key: String) {
    if(this is Player) {
        clearVar(key)
    } else {
        clear(key)
    }
}

fun Character.remaining(key: String): Int {
    return if (this is Player) {
        getVar(key, 0) - epochSeconds()
    } else {
        get(key, 0) - epochSeconds()
    }
}