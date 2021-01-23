package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.utility.func.toInt

data class BooleanVariable(override val id: Int, override val type: Variable.Type, override val persistent: Boolean = false, override val defaultValue: Boolean = false) : Variable<Boolean> {
    override fun toInt(value: Boolean): Int {
        return value.toInt()
    }
}