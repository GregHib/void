package world.gregs.voidps.engine.client.variable

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import world.gregs.voidps.engine.client.sendVarbit
import world.gregs.voidps.engine.client.sendVarc
import world.gregs.voidps.engine.client.sendVarcStr
import world.gregs.voidps.engine.client.sendVarp
import world.gregs.voidps.engine.data.definition.config.VariableDefinition.Companion.persist
import world.gregs.voidps.engine.data.definition.extra.VariableDefinitions
import world.gregs.voidps.engine.data.serial.MapSerializer
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.timer.epochSeconds

class Variables(
    @JsonSerialize(using = MapSerializer::class)
    val variables: MutableMap<String, Any> = mutableMapOf()
) {
    @JsonIgnore
    val temporaryVariables: MutableMap<String, Any> = mutableMapOf()

    @JsonIgnore
    private lateinit var player: Player

    @JsonIgnore
    private var definitions: VariableDefinitions = VariableDefinitions()

    @JsonIgnore
    var bits = VariableBits(this)

    fun link(player: Player, definitions: VariableDefinitions) {
        this.player = player
        this.definitions = definitions
        bits.link(player)
    }

    fun <T : Any> get(key: String): T = getOrNull(key)!!

    fun <T : Any> get(key: String, default: T): T = getOrNull(key) ?: default

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(key: String): T? {
        val variable = definitions.get(key)
        return (store(variable.persist)[key] ?: variable?.defaultValue) as? T
    }

    fun <T : Any> getOrPut(key: String, block: () -> T): T {
        var value = getOrNull<T>(key)
        if (value != null) {
            return value
        }
        value = block.invoke()
        set(key, value, true)
        return value
    }

    fun contains(key: String): Boolean {
        return store(definitions.get(key).persist).containsKey(key)
    }

    fun set(key: String, value: Any, refresh: Boolean) {
        val variable = definitions.get(key)
        if (value == variable?.defaultValue) {
            clear(key, refresh)
            return
        }
        val previous: Any? = getOrNull(key)
        store(variable.persist)[key] = value
        if (refresh) {
            send(key)
        }
        player.events.emit(VariableSet(key, previous, value))
    }

    fun clear(key: String, refresh: Boolean): Any? {
        val variable = definitions.get(key)
        val removed = store(variable.persist).remove(key) ?: return null
        if (refresh) {
            send(key)
        }
        player.events.emit(VariableSet(key, removed, variable?.defaultValue))
        return removed
    }

    fun send(key: String) {
        val variable = definitions.get(key) ?: return
        if (!variable.transmit) {
            return
        }
        val value = get(key, variable.defaultValue)
        when (variable.type) {
            VariableType.Varp -> player.sendVarp(variable.id, variable.format.toInt(variable, value))
            VariableType.Varbit -> player.sendVarbit(variable.id, variable.format.toInt(variable, value))
            VariableType.Varc -> player.sendVarc(variable.id, variable.format.toInt(variable, value))
            VariableType.Varcstr -> player.sendVarcStr(variable.id, value as String)
        }
    }

    internal fun store(persist: Boolean): MutableMap<String, Any> =
        if (persist) variables else temporaryVariables
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