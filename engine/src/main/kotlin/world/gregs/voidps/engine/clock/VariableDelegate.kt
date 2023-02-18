package world.gregs.voidps.engine.clock

import world.gregs.voidps.engine.client.variable.Variables

/**
 * Player clocks names should always be backed by [Variables]
 */
class VariableDelegate(
    val variables: Variables
) : MapDelegate {

    override fun get(name: String): Int? {
        return variables.getOrNull(name)
    }

    override fun set(name: String, value: Int) {
        variables.set(name, value, true)
    }

    override fun remove(name: String) {
        variables.clear(name, true)
    }
}