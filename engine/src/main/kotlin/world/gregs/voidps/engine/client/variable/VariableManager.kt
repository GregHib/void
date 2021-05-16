package world.gregs.voidps.engine.client.variable

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.collections.set

val variablesModule = module {
    single { VariableManager() }
}

@Suppress("UNCHECKED_CAST")
class VariableManager {
    private val variables = mutableMapOf<String, Variable<*>>()

    fun register(name: String, variable: Variable<*>) {
        variables[name] = variable
    }

    fun get(name: String): Variable<*>? = variables[name]

    fun clear() {
        variables.clear()
    }
}

fun <T : Any> Player.setVar(key: String, value: T, refresh: Boolean = true) =
    variables.set(key, value, refresh)

fun Player.sendVar(key: String) = variables.send(key)

fun <T : Any> Player.addVar(key: String, value: T, refresh: Boolean = true) =
    variables.add(key, value, refresh)

fun <T : Any> Player.removeVar(key: String, value: T, refresh: Boolean = true) =
    variables.remove(key, value, refresh)

fun Player.toggleVar(key: String, refresh: Boolean = true): Boolean {
    val value = variables.get(key, false)
    variables.set(key, !value, refresh)
    return !value
}

fun Player.incVar(key: String, refresh: Boolean = true): Int {
    val value: Int = variables.get(key)
    variables.set(key, value + 1, refresh)
    return value + 1
}

fun Player.decVar(key: String, refresh: Boolean = true): Int {
    val value: Int = variables.get(key)
    variables.set(key, value - 1, refresh)
    return value - 1
}

fun <T : Any> Player.hasVar(key: String, id: T): Boolean {
    return variables.has(key, id)
}

fun <T : Any> Player.getVar(key: String, default: T): T {
    return variables.get(key, default)
}

fun <T : Any> Player.getVar(key: String): T {
    return variables.get(key)
}