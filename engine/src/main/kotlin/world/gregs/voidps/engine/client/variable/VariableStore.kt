package world.gregs.voidps.engine.client.variable

import org.koin.dsl.module
import kotlin.collections.set

val variablesModule = module {
    single { VariableStore() }
}

@Suppress("UNCHECKED_CAST")
class VariableStore {
    private val variables = mutableMapOf<String, Variable<*>>()

    fun register(name: String, variable: Variable<*>) {
        variables[name] = variable
    }

    fun get(name: String): Variable<*>? = variables[name]

    fun clear() {
        variables.clear()
    }
}