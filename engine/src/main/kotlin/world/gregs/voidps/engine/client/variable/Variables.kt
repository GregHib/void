package world.gregs.voidps.engine.client.variable

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.data.serial.MapSerializer
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Events

open class Variables(
    @JsonIgnore
    private var events: Events,
    @JsonSerialize(using = MapSerializer::class)
    @JsonProperty("variables")
    val data: VariableData,
) {

    constructor(events: Events, map: MutableMap<String, Any> = mutableMapOf()) : this(events, VariableData(map))

    @JsonIgnore
    var bits = VariableBits(this, events)

    @Suppress("UNCHECKED_CAST")
    open fun <T : Any> get(key: String): T {
        persist(key)
        return data[key] as T
    }

    @Suppress("UNCHECKED_CAST")
    open fun <T : Any> getOrNull(key: String): T? {
        persist(key)
        return data[key] as? T
    }

    open fun <T : Any> get(key: String, default: T): T = getOrNull(key) ?: default

    open fun <T : Any> getOrPut(key: String, block: () -> T): T {
        var value = getOrNull<T>(key)
        if (value != null) {
            return value
        }
        value = block.invoke()
        set(key, value, false)
        return value
    }

    open fun contains(key: String): Boolean {
        persist(key)
        return data.containsKey(key)
    }

    open fun set(key: String, value: Any, refresh: Boolean = true) {
        val previous: Any? = getOrNull(key)
        persist(key)
        data[key] = value
        if (refresh) {
            send(key)
        }
        events.emit(VariableSet(key, previous, value))
    }

    open fun clear(key: String, refresh: Boolean = true): Any? {
        persist(key)
        val removed = data.remove(key) ?: return null
        if (refresh) {
            send(key)
        }
        events.emit(VariableSet(key, removed, null))
        return removed
    }

    open fun send(key: String) {
    }

    open fun persist(key: String) {
        data.persist = false
    }
}

fun Player.setVar(key: String, value: Any, refresh: Boolean = true) =
    variables.set(key, value, refresh)

fun Player.sendVar(key: String) = variables.send(key)

fun Player.addVar(key: String, value: Any, refresh: Boolean = true) =
    variables.bits.set(key, value, refresh)

fun Player.removeVarbit(key: String, value: Any, refresh: Boolean = true) =
    variables.bits.remove(key, value, refresh)

fun <T : Any> Character.removeVar(key: String, refresh: Boolean = true) =
    variables.clear(key, refresh) as? T

fun Character.clearVar(key: String, refresh: Boolean = true) =
    variables.clear(key, refresh)

fun Player.toggleVar(key: String, refresh: Boolean = true): Boolean {
    val value = variables.get(key, false)
    variables.set(key, !value as Any, refresh)
    return !value
}

fun Character.incVar(key: String, amount: Int = 1, refresh: Boolean = true): Int {
    val value: Int = variables.get(key, 0)
    variables.set(key, value + amount, refresh)
    return value + amount
}

fun Player.decVar(key: String, amount: Int = 1, refresh: Boolean = true): Int {
    val value: Int = variables.get(key, 0)
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

fun Character.start(key: String, duration: Int, base: Int = GameLoop.tick) {
    if (duration == -1) {
        variables.set(key, duration)
    } else {
        variables.set(key, base + duration)
    }
}

fun Character.stop(key: String) {
    variables.clear(key)
}

fun Character.hasClock(key: String, base: Int = GameLoop.tick): Boolean {
    val tick: Int = variables.getOrNull(key) ?: return false
    if (tick == -1) {
        return true
    }
    return tick > base
}

fun Character.remaining(key: String, base: Int = GameLoop.tick): Int {
    val tick: Int = variables.getOrNull(key) ?: return -1
    if (tick == -1) {
        return -1
    }
    if (tick <= base) {
        stop(key)
    }
    return tick - base
}