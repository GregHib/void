package world.gregs.voidps.engine.client.variable

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import world.gregs.voidps.engine.data.definition.config.VariableDefinition.Companion.persist
import world.gregs.voidps.engine.data.definition.extra.VariableDefinitions
import world.gregs.voidps.engine.data.serial.MapSerializer
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.sendVarbit
import world.gregs.voidps.network.encode.sendVarc
import world.gregs.voidps.network.encode.sendVarcStr
import world.gregs.voidps.network.encode.sendVarp

open class Variables(
    @JsonSerialize(using = MapSerializer::class)
    @JsonProperty("variables")
    val data: VariableData,
    @JsonIgnore
    private var events: Events,
    @JsonIgnore
    var definitions: VariableDefinitions = VariableDefinitions()
) {

    constructor(map: MutableMap<String, Any>, events: Events) : this(VariableData(map), events)

    @JsonIgnore
    var client: Client? = null

    @JsonIgnore
    var bits = VariableBits(this, events)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String): T {
        val variable = definitions.get(key)
        data.persist = variable.persist
        return (data[key] ?: variable?.defaultValue) as T
    }

    fun <T : Any> get(key: String, default: T): T = getOrNull(key) ?: default

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(key: String): T? {
        val variable = definitions.get(key)
        data.persist = variable.persist
        return data[key] as? T
    }

    fun <T : Any> getOrPut(key: String, block: () -> T): T {
        var value = getOrNull<T>(key)
        if (value != null) {
            return value
        }
        value = block.invoke()
        set(key, value, false)
        return value
    }

    fun contains(key: String): Boolean {
        data.persist = definitions.get(key).persist
        return data.containsKey(key)
    }

    fun set(key: String, value: Any, refresh: Boolean) {
        val variable = definitions.get(key)
        if (value == variable?.defaultValue) {
            clear(key, refresh)
            return
        }
        val previous: Any? = getOrNull(key) ?: variable?.defaultValue
        data.persist = variable.persist
        data[key] = value
        if (refresh) {
            send(key)
        }
        events.emit(VariableSet(key, previous, value))
    }

    fun clear(key: String, refresh: Boolean): Any? {
        val variable = definitions.get(key)
        data.persist = variable.persist
        val removed = data.remove(key) ?: return null
        if (refresh) {
            send(key)
        }
        events.emit(VariableSet(key, removed, variable?.defaultValue))
        return removed
    }

    fun send(key: String) {
        val variable = definitions.get(key) ?: return
        if (!variable.transmit) {
            return
        }
        val value = get(key, variable.defaultValue)
        when (variable.type) {
            VariableType.Varp -> client?.sendVarp(variable.id, variable.format.toInt(variable, value))
            VariableType.Varbit -> client?.sendVarbit(variable.id, variable.format.toInt(variable, value))
            VariableType.Varc -> client?.sendVarc(variable.id, variable.format.toInt(variable, value))
            VariableType.Varcstr -> client?.sendVarcStr(variable.id, value as String)
        }
    }
}

fun Player.setVar(key: String, value: Any, refresh: Boolean = true) =
    variables.set(key, value, refresh)

fun Player.sendVar(key: String) = variables.send(key)

fun Player.addVar(key: String, value: Any, refresh: Boolean = true) =
    variables.bits.set(key, value, refresh)

fun Player.removeVar(key: String, value: Any, refresh: Boolean = true) =
    variables.bits.remove(key, value, refresh)

fun Player.clearVar(key: String, refresh: Boolean = true) =
    variables.clear(key, refresh)

fun Player.toggleVar(key: String, refresh: Boolean = true): Boolean {
    val value = variables.get(key, false)
    variables.set(key, !value as Any, refresh)
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

fun Player.hasVar(key: String, id: Any): Boolean {
    return variables.bits.contains(key, id)
}

fun Player.hasVar(key: String): Boolean {
    return variables.contains(key)
}

fun <T : Any> Player.getVar(key: String, default: T): T {
    return variables.get(key, default)
}

fun <T : Any> Player.getVar(key: String): T {
    return variables.get(key)
}

fun Character.start(key: String, seconds: Int) {
    if (this is Player) {
        setVar(key, epochSeconds() + seconds)
    } else {
        this[key] = epochSeconds() + seconds
    }
}

fun Character.stop(key: String) {
    if (this is Player) {
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